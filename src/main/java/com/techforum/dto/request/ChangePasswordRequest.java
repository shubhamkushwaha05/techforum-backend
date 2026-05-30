package com.techforum.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "Current password is required") private String currentPassword;
    @NotBlank(message = "New password is required") @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;
}
