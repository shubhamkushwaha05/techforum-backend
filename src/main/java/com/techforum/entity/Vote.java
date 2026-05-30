package com.techforum.entity;
import com.techforum.enums.VoteType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Entity @Table(name = "votes", uniqueConstraints = { @UniqueConstraint(columnNames = {"user_id", "post_id"}) })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "post_id", nullable = false) private Post post;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private VoteType voteType;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
}
