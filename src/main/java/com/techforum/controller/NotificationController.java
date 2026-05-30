package com.techforum.controller;

import com.techforum.dto.response.ApiResponse;
import com.techforum.dto.response.NotificationResponse;
import com.techforum.dto.response.PagedResponse;
import com.techforum.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // GET /api/notifications?unreadOnly=false&page=0&size=20
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>>
            getNotifications(
                @RequestParam(defaultValue = "false") boolean unreadOnly,
                @RequestParam(defaultValue = "0")     int     page,
                @RequestParam(defaultValue = "20")    int     size,
                @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success(
                "Notifications fetched",
                notificationService.getMyNotifications(
                        userDetails.getUsername(), unreadOnly, page, size)));
    }

    // GET /api/notifications/unread-count
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success(
                "Unread count",
                notificationService.getUnreadCount(
                        userDetails.getUsername())));
    }

    // PUT /api/notifications/{id}/read
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        notificationService.markAsRead(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Marked as read"));
    }

    // PUT /api/notifications/read-all
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {

        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("All notifications marked as read"));
    }

    // DELETE /api/notifications/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        notificationService.deleteNotification(id, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Notification deleted"));
    }
}
