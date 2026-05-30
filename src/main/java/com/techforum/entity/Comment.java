package com.techforum.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity @Table(name = "comments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id", nullable = false) private User author;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "post_id") private Post post;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "answer_id") private Answer answer;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_id") private Comment parent;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default private List<Comment> replies = new ArrayList<>();
    @Builder.Default @Column(name = "vote_count") private Integer voteCount = 0;
    @Builder.Default @Column(name = "is_deleted") private Boolean isDeleted = false;
    @Column(name = "mentioned_user", length = 50) private String mentionedUser;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private LocalDateTime updatedAt;
}
