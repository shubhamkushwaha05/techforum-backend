package com.techforum.service;
import com.techforum.dto.response.NotificationResponse;
import com.techforum.dto.response.PagedResponse;
import com.techforum.entity.User;
import com.techforum.enums.NotificationType;
public interface NotificationService {
    void createNotification(User recipient, User actor, NotificationType type,
                            String message, String link, Long referenceId, String referenceType);
    PagedResponse<NotificationResponse> getMyNotifications(String username, boolean unreadOnly, int page, int size);
    Long getUnreadCount(String username);
    void markAsRead(Long notificationId, String username);
    void markAllAsRead(String username);
    void deleteNotification(Long notificationId, String username);
}
