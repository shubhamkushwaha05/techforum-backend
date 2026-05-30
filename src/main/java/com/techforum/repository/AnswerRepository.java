package com.techforum.repository;

import com.techforum.entity.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    // Get all answers for a post — verified first, then by votes
    @Query("""
        SELECT a FROM Answer a
        WHERE a.post.id = :postId
        AND a.isDeleted = false
        ORDER BY a.isVerifiedAnswer DESC,
                 a.voteCount DESC,
                 a.createdAt ASC
    """)
    List<Answer> findByPostIdOrderByVerifiedAndVotes(@Param("postId") Long postId);

    // Paginated answers for a post
    Page<Answer> findByPostIdAndIsDeletedFalseOrderByIsVerifiedAnswerDescVoteCountDesc(
            Long postId, Pageable pageable);

    // Get answers by author
    Page<Answer> findByAuthorIdAndIsDeletedFalse(Long authorId, Pageable pageable);

    // Check if verified answer exists for a post
    Boolean existsByPostIdAndIsVerifiedAnswerTrue(Long postId);

    // Count answers for a post
    Long countByPostIdAndIsDeletedFalse(Long postId);

    // Count answers by a specific author (non-deleted)
    Long countByAuthorIdAndIsDeletedFalse(Long authorId);

    // Get verified answer for a post
    @Query("""
        SELECT a FROM Answer a
        WHERE a.post.id = :postId
        AND a.isVerifiedAnswer = true
        AND a.isDeleted = false
    """)
    Optional<Answer> findVerifiedAnswerByPostId(@Param("postId") Long postId);

    // FIX: COUNT of verified answers for a specific author.
    //      Previously UserProfileServiceImpl used answerRepository.findAll()
    //      + Java stream filter — which loads every answer in the DB into memory.
    @Query("""
        SELECT COUNT(a) FROM Answer a
        WHERE a.author.id = :authorId
        AND a.isVerifiedAnswer = true
        AND a.isDeleted = false
    """)
    long countVerifiedByAuthorId(@Param("authorId") Long authorId);

    // FIX: COUNT of all verified answers across the platform (for ModeratorServiceImpl stats).
    //      Previously used findAll().stream().filter() — O(N) heap allocation.
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.isVerifiedAnswer = true AND a.isDeleted = false")
    long countAllVerified();
}
