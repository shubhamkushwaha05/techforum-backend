package com.techforum.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserSummaryResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
    private Integer reputationPoints;
    private Boolean isActive;
    private Boolean isBanned;
    private Boolean emailVerified;
    private Long postCount;
    private Long warningCount;
    private LocalDateTime createdAt;
}
