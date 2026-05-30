package com.techforum.dto.response;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponse {
    private Long id;
    private String type;
    private String message;
    private String link;
    private Boolean isRead;
    private String actorUsername;
    private String actorProfileImage;
    private Long referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
}
