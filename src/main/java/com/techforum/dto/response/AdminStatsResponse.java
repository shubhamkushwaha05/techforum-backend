package com.techforum.dto.response;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminStatsResponse {
    private Long totalUsers;
    private Long totalPosts;
    private Long totalAnswers;
    private Long pendingReports;
    private Long bannedUsers;
    private Long solvedPosts;
    private Long totalModerators;
    private Long totalAdmins;
}
