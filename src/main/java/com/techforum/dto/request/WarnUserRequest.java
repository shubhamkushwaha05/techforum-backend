package com.techforum.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WarnUserRequest {
    @NotBlank(message = "Reason is required") private String reason;
}
