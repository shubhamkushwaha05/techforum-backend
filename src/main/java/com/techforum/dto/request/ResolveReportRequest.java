package com.techforum.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ResolveReportRequest {
    @NotBlank(message = "Action is required: RESOLVED or DISMISSED") private String action;
    private String resolutionNote;
}
