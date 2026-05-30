package com.techforum.dto.request;
import jakarta.validation.constraints.Size;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateProfileRequest {
    @Size(min = 2, max = 100, message = "Full name must be 2-100 characters") private String fullName;
    @Size(max = 300, message = "Bio must be under 300 characters") private String bio;
    private String profileImage;
}
