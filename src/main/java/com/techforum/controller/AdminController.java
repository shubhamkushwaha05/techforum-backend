package com.techforum.controller;

import com.techforum.dto.response.*;
import com.techforum.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // GET /api/admin/stats
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(
                "Stats fetched", adminService.getStats()));
    }

    // GET /api/admin/users?page=0&size=20
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PagedResponse<UserSummaryResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                "Users fetched", adminService.getAllUsers(page, size)));
    }

    // GET /api/admin/users/search?keyword=john&page=0&size=20
    // FIX: Now uses DB-level LIKE query — correct pagination for search results.
    //      Previously loaded all users into memory and filtered in Java.
    @GetMapping("/users/search")
    public ResponseEntity<ApiResponse<PagedResponse<UserSummaryResponse>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success("Search results",
                adminService.searchUsers(keyword, page, size)));
    }

    // GET /api/admin/users/{userId}
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getUserDetail(
            @PathVariable Long userId) {

        return ResponseEntity.ok(ApiResponse.success(
                "User detail", adminService.getUserDetail(userId)));
    }

    // POST /api/admin/users/{userId}/ban
    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<ApiResponse<Void>> banUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        adminService.banUser(userId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User banned"));
    }

    // POST /api/admin/users/{userId}/unban
    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<ApiResponse<Void>> unbanUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        adminService.unbanUser(userId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User unbanned"));
    }

    // POST /api/admin/users/{userId}/promote
    @PostMapping("/users/{userId}/promote")
    public ResponseEntity<ApiResponse<Void>> promote(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        adminService.promoteToModerator(userId, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("User promoted to moderator"));
    }

    // POST /api/admin/users/{userId}/demote
    @PostMapping("/users/{userId}/demote")
    public ResponseEntity<ApiResponse<Void>> demote(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        adminService.demoteFromModerator(userId, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Moderator role removed"));
    }

    // DELETE /api/admin/posts/{postId}
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        adminService.deletePost(postId, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Post deleted by admin"));
    }
}
