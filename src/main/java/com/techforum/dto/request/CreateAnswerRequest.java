package com.techforum.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateAnswerRequest {
    @NotBlank(message = "Answer content is required")
    @Size(min = 20, max = 10000, message = "Answer must be between 20 and 10000 characters")
    private String content;
}
