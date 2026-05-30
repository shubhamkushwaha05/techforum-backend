package com.techforum.repository;

import com.techforum.entity.Post;
import com.techforum.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Fix 3: Added findByIsDeletedFalse(Pageable) for dynamic sorting.
    // Methods with OrderBy in the name conflict with Pageable sort —
    // behavior is undefined. Use this method when sort comes from Pageable.
    Page<Post> findByIsDeletedFalse(Pageable pageable);

    // Used for default "newest" + pinned-first sort (fixed ORDER BY is intentional)
    Page<Post> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
    Page<Post> findByIsDeletedFalseOrderByIsPinnedDescCreatedAtDesc(Pageable pageable);

    Page<Post> findByAuthorIdAndIsDeletedFalse(Long authorId, Pageable pageable);
    Page<Post> findByStatusAndIsDeletedFalse(PostStatus status, Pageable pageable);

    @Query("""
        SELECT p FROM Post p
        WHERE p.isDeleted = false
        AND (LOWER(p.title)   LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY p.createdAt DESC
    """)
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT p FROM Post p JOIN p.tags t
        WHERE p.isDeleted = false AND LOWER(t.name) = LOWER(:tagName)
        ORDER BY p.createdAt DESC
    """)
    Page<Post> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    @Query("""
        SELECT p FROM Post p
        WHERE p.isDeleted = false AND p.createdAt >= :since
        ORDER BY p.voteCount DESC, p.viewCount DESC
    """)
    Page<Post> findTrending(@Param("since") java.time.LocalDateTime since, Pageable pageable);

    @Query("""
        SELECT p FROM Post p
        WHERE p.isDeleted = false
        AND LOWER(p.author.username) LIKE LOWER(CONCAT('%', :username, '%'))
        ORDER BY p.createdAt DESC
    """)
    Page<Post> findByAuthorUsername(@Param("username") String username, Pageable pageable);

    Long countByAuthorIdAndIsDeletedFalse(Long authorId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.hasVerifiedAnswer = true AND p.isDeleted = false")
    long countSolvedPosts();

    @Query("SELECT COUNT(p) FROM Post p WHERE p.isPinned = true AND p.isDeleted = false")
    long countPinnedPosts();
}
