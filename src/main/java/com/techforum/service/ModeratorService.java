package com.techforum.service;
import com.techforum.dto.request.ResolveReportRequest;
import com.techforum.dto.request.WarnUserRequest;
import com.techforum.dto.response.*;
public interface ModeratorService {
    PagedResponse<ReportResponse> getReports(String status, int page, int size);
    ReportResponse resolveReport(Long reportId, ResolveReportRequest request, String moderatorUsername);
    void pinPost(Long postId, String moderatorUsername);
    void unpinPost(Long postId, String moderatorUsername);
    void warnUser(Long userId, WarnUserRequest request, String moderatorUsername);
    ModeratorStatsResponse getStats();
    PagedResponse<ReportResponse> getMyActions(String moderatorUsername, int page, int size);
}
