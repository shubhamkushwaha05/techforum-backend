package com.techforum.dto.response;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModeratorStatsResponse {
    private Long pendingReports;
    private Long verifiedAnswersTotal;
    private Long warningsIssuedTotal;
    private Long pinnedPostsTotal;
    private Long myActionsToday;
}
