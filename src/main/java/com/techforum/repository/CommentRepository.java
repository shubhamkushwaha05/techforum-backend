package com.techforum.repository;
import com.techforum.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL AND c.isDeleted = false ORDER BY c.createdAt ASC")
    List<Comment> findTopLevelCommentsByPostId(@Param("postId") Long postId);
    @Query("SELECT c FROM Comment c WHERE c.answer.id = :answerId AND c.parent IS NULL AND c.isDeleted = false ORDER BY c.createdAt ASC")
    List<Comment> findTopLevelCommentsByAnswerId(@Param("answerId") Long answerId);
    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId AND c.isDeleted = false ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);
    Long countByPostIdAndIsDeletedFalse(Long postId);
    Long countByAnswerIdAndIsDeletedFalse(Long answerId);
}
