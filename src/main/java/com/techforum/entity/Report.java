package com.techforum.entity;
import com.techforum.enums.ReportStatus;
import com.techforum.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
@Entity @Table(name = "reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reporter_id", nullable = false) private User reporter;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ReportType reportType;
    @Column(name = "target_id", nullable = false) private Long targetId;
    @Column(columnDefinition = "TEXT") private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ReportStatus status = ReportStatus.PENDING;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "resolved_by") private User resolvedBy;
    @Column(name = "resolution_note", columnDefinition = "TEXT") private String resolutionNote;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private LocalDateTime updatedAt;
}
