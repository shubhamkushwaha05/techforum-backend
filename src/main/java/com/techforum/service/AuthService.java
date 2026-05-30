package com.techforum.service;
import com.techforum.dto.request.LoginRequest;
import com.techforum.dto.request.RegisterRequest;
import com.techforum.dto.response.AuthResponse;
public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
