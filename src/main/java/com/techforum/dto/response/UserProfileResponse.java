package com.techforum.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String profileImage;
    private String bio;
    private Integer reputationPoints;
    private Set<String> roles;
    private Boolean isActive;
    private Boolean isBanned;
    private Boolean emailVerified;
    private Long totalPosts;
    private Long totalAnswers;
    private Long totalVerifiedAnswers;
    private Long warningCount;
    private String reputationBadge;
    private LocalDateTime createdAt;
}
