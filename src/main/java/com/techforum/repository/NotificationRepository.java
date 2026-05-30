package com.techforum.repository;
import com.techforum.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    Page<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    Long countByRecipientIdAndIsReadFalse(Long recipientId);
    @Modifying @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);
    @Modifying @Transactional
    void deleteByRecipientId(Long recipientId);
}
