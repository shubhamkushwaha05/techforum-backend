package com.techforum.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 50) private String username;
    @Column(nullable = false, unique = true, length = 100) private String email;
    @Column(nullable = false) private String password;
    @Column(name = "full_name", length = 100) private String fullName;
    @Column(name = "profile_image") private String profileImage;
    @Column(columnDefinition = "TEXT") private String bio;
    @Builder.Default @Column(name = "reputation_points") private Integer reputationPoints = 0;
    @Builder.Default @Column(name = "is_active") private Boolean isActive = true;
    @Builder.Default @Column(name = "is_banned") private Boolean isBanned = false;
    @Column(name = "email_verified") private Boolean emailVerified = false;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private LocalDateTime updatedAt;
}
