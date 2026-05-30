package com.techforum.repository;
import com.techforum.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserIdAndPostId(Long userId, Long postId);
    Boolean existsByUserIdAndPostId(Long userId, Long postId);
    Page<Bookmark> findByUserId(Long userId, Pageable pageable);
}
