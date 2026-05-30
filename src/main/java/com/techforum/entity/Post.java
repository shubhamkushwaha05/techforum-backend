package com.techforum.entity;
import com.techforum.enums.PostStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.*;
@Entity @Table(name = "posts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 300) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id", nullable = false) private User author;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();
    @Enumerated(EnumType.STRING) @Column(name = "status") private PostStatus status = PostStatus.OPEN;
    @Builder.Default @Column(name = "vote_count") private Integer voteCount = 0;
    @Builder.Default @Column(name = "view_count") private Integer viewCount = 0;
    @Builder.Default @Column(name = "answer_count") private Integer answerCount = 0;
    @Builder.Default @Column(name = "is_pinned") private Boolean isPinned = false;
    @Builder.Default @Column(name = "has_verified_answer") private Boolean hasVerifiedAnswer = false;
    @Column(name = "is_deleted") private Boolean isDeleted = false;
    @Column(name = "moderation_status", length = 50) private String moderationStatus = "APPROVED";
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private LocalDateTime updatedAt;
}
