package com.techforum.entity;
import com.techforum.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Entity @Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recipient_id", nullable = false) private User recipient;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "actor_id") private User actor;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private NotificationType type;
    @Column(nullable = false) private String message;
    @Column(name = "link") private String link;
    @Builder.Default @Column(name = "is_read") private Boolean isRead = false;
    @Column(name = "reference_id") private Long referenceId;
    @Column(name = "reference_type", length = 50) private String referenceType;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
}
