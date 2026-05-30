package com.techforum.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateCommentRequest {
    @NotBlank(message = "Comment content is required") @Size(min = 2, max = 2000)
    private String content;
}
