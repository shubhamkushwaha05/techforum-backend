package com.techforum.service.impl;

import com.techforum.dto.request.LoginRequest;
import com.techforum.dto.request.RegisterRequest;
import com.techforum.dto.response.AuthResponse;
import com.techforum.entity.Role;
import com.techforum.entity.User;
import com.techforum.enums.RoleName;
import com.techforum.exception.BadRequestException;
import com.techforum.exception.ResourceNotFoundException;
import com.techforum.repository.RoleRepository;
import com.techforum.repository.UserRepository;
import com.techforum.security.jwt.JwtUtils;
import com.techforum.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired private UserRepository     userRepository;
    @Autowired private RoleRepository     roleRepository;
    @Autowired private PasswordEncoder    passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtils           jwtUtils;

    // ── REGISTER ───────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Validate uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Default role not found. Run data seeder first."));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .roles(roles)
                .reputationPoints(0)
                .isActive(true)
                .isBanned(false)
                .emailVerified(false)
                .build();

        userRepository.save(user);

        // Auto-login after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        Set<String> roleNames = roles.stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roleNames)
                .reputationPoints(user.getReputationPoints())
                .build();
    }

    // ── LOGIN ──────────────────────────────────────────────
    @Override
    public AuthResponse login(LoginRequest request) {

        // FIX: Spring Security will throw BadCredentialsException for wrong
        //      credentials and UsernameNotFoundException (wrapped as
        //      BadCredentialsException) for banned/unknown users.
        //      These are now caught by GlobalExceptionHandler and returned
        //      as a clean 401 with "Invalid username or password".
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Load full user details for the response
        User user = userRepository.findByUsername(authentication.getName())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found after authentication"));

        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .reputationPoints(user.getReputationPoints())
                .build();
    }
}
