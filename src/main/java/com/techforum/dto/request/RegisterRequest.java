package com.techforum.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, underscores")
    private String username;
    @NotBlank(message = "Email is required") @Email(message = "Please provide a valid email")
    private String email;
    @NotBlank(message = "Password is required") @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;
    @NotBlank(message = "Full name is required") @Size(min = 2, max = 100)
    private String fullName;
}
