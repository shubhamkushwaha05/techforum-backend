package com.techforum.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateCommentRequest {
    @NotBlank(message = "Comment content is required")
    @Size(min = 2, max = 2000, message = "Comment must be between 2 and 2000 characters")
    private String content;
    private Long parentId;
    private String mentionedUser;
}
