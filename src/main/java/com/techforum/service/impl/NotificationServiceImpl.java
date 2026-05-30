package com.techforum.service.impl;

import com.techforum.dto.response.NotificationResponse;
import com.techforum.dto.response.PagedResponse;
import com.techforum.entity.Notification;
import com.techforum.entity.User;
import com.techforum.enums.NotificationType;
import com.techforum.exception.BadRequestException;
import com.techforum.exception.ResourceNotFoundException;
import com.techforum.repository.NotificationRepository;
import com.techforum.repository.UserRepository;
import com.techforum.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository         userRepository;

    // ── CREATE NOTIFICATION ────────────────────────────────
    @Override
    @Transactional
    public void createNotification(User recipient, User actor,
                                    NotificationType type, String message,
                                    String link, Long referenceId,
                                    String referenceType) {
        // Don't notify users of their own actions
        if (actor != null && actor.getId().equals(recipient.getId())) return;

        // FIX: Guard against null recipient to prevent NullPointerException
        //      if a caller accidentally passes a null recipient.
        if (recipient == null) return;

        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .message(message)
                .link(link)
                .isRead(false)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();

        notificationRepository.save(notification);
    }

    // ── GET MY NOTIFICATIONS ───────────────────────────────
    @Override
    public PagedResponse<NotificationResponse> getMyNotifications(
            String username, boolean unreadOnly, int page, int size) {

        User user = getUser(username);
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications;

        if (unreadOnly) {
            notifications = notificationRepository
                    .findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(
                            user.getId(), pageable);
        } else {
            notifications = notificationRepository
                    .findByRecipientIdOrderByCreatedAtDesc(
                            user.getId(), pageable);
        }

        List<NotificationResponse> content = notifications.getContent()
                .stream().map(this::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<NotificationResponse>builder()
                .content(content)
                .pageNumber(notifications.getNumber())
                .pageSize(notifications.getSize())
                .totalElements(notifications.getTotalElements())
                .totalPages(notifications.getTotalPages())
                .last(notifications.isLast())
                .build();
    }

    // ── GET UNREAD COUNT ───────────────────────────────────
    @Override
    public Long getUnreadCount(String username) {
        User user = getUser(username);
        return notificationRepository
                .countByRecipientIdAndIsReadFalse(user.getId());
    }

    // ── MARK SINGLE AS READ ────────────────────────────────
    @Override
    @Transactional
    public void markAsRead(Long notificationId, String username) {
        User user = getUser(username);
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found: " + notificationId));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new BadRequestException("Not your notification");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    // ── MARK ALL AS READ ───────────────────────────────────
    @Override
    @Transactional
    public void markAllAsRead(String username) {
        User user = getUser(username);
        notificationRepository.markAllAsReadByUserId(user.getId());
    }

    // ── DELETE NOTIFICATION ────────────────────────────────
    @Override
    @Transactional
    public void deleteNotification(Long notificationId, String username) {
        User user = getUser(username);
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found"));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new BadRequestException("Not your notification");
        }
        notificationRepository.delete(notification);
    }

    // ── HELPERS ────────────────────────────────────────────
    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .message(n.getMessage())
                .link(n.getLink())
                .isRead(n.getIsRead())
                .actorUsername(n.getActor() != null
                        ? n.getActor().getUsername() : null)
                .actorProfileImage(n.getActor() != null
                        ? n.getActor().getProfileImage() : null)
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));
    }
}
