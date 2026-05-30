package com.techforum.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Entity @Table(name = "moderation_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModerationLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "moderator_id", nullable = false) private User moderator;
    @Column(nullable = false, length = 100) private String action;
    @Column(name = "target_type", length = 50) private String targetType;
    @Column(name = "target_id") private Long targetId;
    @Column(columnDefinition = "TEXT") private String details;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
}
