package com.techforum.controller;

import com.techforum.dto.request.LoginRequest;
import com.techforum.dto.request.RegisterRequest;
import com.techforum.dto.response.ApiResponse;
import com.techforum.dto.response.AuthResponse;
import com.techforum.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(
                ApiResponse.success("User registered successfully", response));
    }

    // POST /api/auth/login
    // FIX: BadCredentialsException from Spring Security is now caught in
    //      GlobalExceptionHandler and returns a clean 401 "Invalid username
    //      or password" — no internal detail is leaked.
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response));
    }

    // GET /api/auth/health
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Auth service is running", "OK"));
    }
}
