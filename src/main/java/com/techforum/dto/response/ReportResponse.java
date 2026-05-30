package com.techforum.dto.response;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportResponse {
    private Long id;
    private String reporterUsername;
    private String reportType;
    private Long targetId;
    private String reason;
    private String status;
    private String resolvedByUsername;
    private String resolutionNote;
    private LocalDateTime createdAt;
}
