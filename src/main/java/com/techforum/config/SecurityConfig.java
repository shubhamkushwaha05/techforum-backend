package com.techforum.config;

import com.techforum.security.UserDetailsServiceImpl;
import com.techforum.security.jwt.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    // FIX: Read allowed origins from environment variable so production can
    //      restrict to the actual frontend domain (e.g. https://techforum.com).
    //      Defaults to localhost for dev convenience.
    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOriginsRaw;

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // FIX: CORS now reads origins from config instead of using wildcard "*".
    //      allowedOriginPatterns("*") with allowCredentials(true) works but is
    //      insecure in production — anyone can make credentialed requests.
    //      Set CORS_ALLOWED_ORIGINS=https://yourdomain.com in production env.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = Arrays.asList(allowedOriginsRaw.split(","));
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Cache preflight for 1 hour

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public auth endpoints
                .requestMatchers("/api/auth/**").permitAll()

                // Fix 5: Specific authenticated rules BEFORE wildcard permitAll.
                // /api/posts/bookmarks/me needs auth — must come first or the
                // GET /api/posts/** wildcard would allow unauthenticated access.
                .requestMatchers(HttpMethod.GET, "/api/posts/bookmarks/me").authenticated()
                // Public read-only endpoints
                .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users/*/profile").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users/*/posts").permitAll()

                // FIX: Answers and comments on answers are also publicly readable.
                //      Previously GET /api/answers/** was not permitted, meaning
                //      unauthenticated users couldn't see answers on a post page.
                .requestMatchers(HttpMethod.GET, "/api/answers/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts/*/comments").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/answers/*/comments").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/comments/*/replies").permitAll()

                // Role-restricted dashboards
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/moderator/**").hasAnyRole("MODERATOR", "ADMIN")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter(),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
