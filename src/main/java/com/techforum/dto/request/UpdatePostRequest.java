package com.techforum.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Set;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdatePostRequest {
    @NotBlank(message = "Title is required") @Size(min = 10, max = 300) private String title;
    @NotBlank(message = "Content is required") @Size(min = 20) private String content;
    @Size(max = 5, message = "Maximum 5 tags allowed") private Set<String> tags;
}
