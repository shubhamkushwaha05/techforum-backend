package com.techforum.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Set;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreatePostRequest {
    @NotBlank(message = "Title is required") @Size(min = 10, max = 300, message = "Title must be between 10 and 300 characters")
    private String title;
    @NotBlank(message = "Content is required") @Size(min = 20, message = "Content must be at least 20 characters")
    private String content;
    @Size(max = 5, message = "Maximum 5 tags allowed") private Set<String> tags;
}
