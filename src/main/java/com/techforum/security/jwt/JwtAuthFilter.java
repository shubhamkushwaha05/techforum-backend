package com.techforum.security.jwt;

import com.techforum.security.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired private JwtUtils               jwtUtils;
    @Autowired private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest  request,
            HttpServletResponse response,
            FilterChain         filterChain) throws ServletException, IOException {

        try {
            String jwt = parseJwt(request);

            // FIX: explicit null/blank guard before hitting the JWT library —
            //      avoids an IllegalArgumentException log-spam on anonymous requests.
            if (StringUtils.hasText(jwt) && jwtUtils.validateJwtToken(jwt)) {

                String username = jwtUtils.getUsernameFromToken(jwt);

                // FIX: skip authentication if context already has a principal
                //      (shouldn't happen with stateless, but guards against edge cases)
                if (SecurityContextHolder.getContext()
                        .getAuthentication() == null) {

                    UserDetails userDetails =
                            userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null,
                                    userDetails.getAuthorities());

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }
        } catch (UsernameNotFoundException e) {
            // User in token no longer exists or is banned — silently reject,
            // downstream will return 401 through the security chain.
            logger.debug("User from token not found or banned: {}",
                    e.getMessage());
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /** Extract Bearer token from Authorization header. */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth)
                && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
