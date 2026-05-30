package com.techforum.repository;
import com.techforum.entity.Vote;
import com.techforum.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserIdAndPostId(Long userId, Long postId);
    Long countByPostIdAndVoteType(Long postId, VoteType voteType);
    Boolean existsByUserIdAndPostId(Long userId, Long postId);
}
