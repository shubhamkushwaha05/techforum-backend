package com.techforum.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
@Entity @Table(name = "answers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Answer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "post_id", nullable = false) private Post post;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id", nullable = false) private User author;
    @Builder.Default @Column(name = "vote_count") private Integer voteCount = 0;
    @Builder.Default @Column(name = "is_verified_answer") private Boolean isVerifiedAnswer = false;
    @Column(name = "verified_by") private Long verifiedBy;
    @Column(name = "verified_at") private LocalDateTime verifiedAt;
    @Builder.Default @Column(name = "is_deleted") private Boolean isDeleted = false;
    @Builder.Default @Column(name = "moderation_status", length = 50) private String moderationStatus = "APPROVED";
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private LocalDateTime updatedAt;
}
