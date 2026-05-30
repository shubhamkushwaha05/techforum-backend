package com.techforum.repository;
import com.techforum.entity.AnswerVote;
import com.techforum.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface AnswerVoteRepository extends JpaRepository<AnswerVote, Long> {
    Optional<AnswerVote> findByUserIdAndAnswerId(Long userId, Long answerId);
    Long countByAnswerIdAndVoteType(Long answerId, VoteType voteType);
    Boolean existsByUserIdAndAnswerId(Long userId, Long answerId);
}
