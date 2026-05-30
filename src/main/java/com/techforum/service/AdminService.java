package com.techforum.service;
import com.techforum.dto.response.*;
public interface AdminService {
    AdminStatsResponse getStats();
    PagedResponse<UserSummaryResponse> getAllUsers(int page, int size);
    UserSummaryResponse getUserDetail(Long userId);
    void banUser(Long userId, String adminUsername);
    void unbanUser(Long userId, String adminUsername);
    void deletePost(Long postId, String adminUsername);
    void promoteToModerator(Long userId, String adminUsername);
    void demoteFromModerator(Long userId, String adminUsername);
    PagedResponse<UserSummaryResponse> searchUsers(String keyword, int page, int size);
}
