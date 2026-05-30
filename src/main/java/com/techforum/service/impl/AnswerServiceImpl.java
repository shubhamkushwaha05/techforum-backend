package com.techforum.service.impl;

import com.techforum.config.AnswerMapper;
import com.techforum.dto.request.CreateAnswerRequest;
import com.techforum.dto.request.UpdateAnswerRequest;
import com.techforum.dto.response.AnswerResponse;
import com.techforum.dto.response.CommentResponse;
import com.techforum.entity.*;
import com.techforum.enums.PostStatus;
import com.techforum.enums.VoteType;
import com.techforum.exception.BadRequestException;
import com.techforum.exception.ResourceNotFoundException;
import com.techforum.repository.*;
import com.techforum.service.AnswerService;
import com.techforum.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnswerServiceImpl implements AnswerService {

    @Autowired private AnswerRepository     answerRepository;
    @Autowired private PostRepository       postRepository;
    @Autowired private UserRepository       userRepository;
    @Autowired private CommentRepository    commentRepository;
    @Autowired private AnswerVoteRepository answerVoteRepository;
    @Autowired private AnswerMapper         answerMapper;
    @Autowired private NotificationService  notificationService;

    // ── CREATE ANSWER ──────────────────────────────────────
    @Override
    @Transactional
    public AnswerResponse createAnswer(Long postId,
                                       CreateAnswerRequest request,
                                       String username) {
        Post post   = getActivePost(postId);
        User author = getUser(username);

        if (post.getStatus() == PostStatus.CLOSED
                || post.getStatus() == PostStatus.DELETED) {
            throw new BadRequestException(
                    "Cannot answer a closed or deleted post");
        }

        Answer answer = Answer.builder()
                .content(request.getContent())
                .post(post)
                .author(author)
                .voteCount(0)
                .isVerifiedAnswer(false)
                .isDeleted(false)
                .moderationStatus("APPROVED")
                .build();

        Answer saved = answerRepository.save(answer);

        post.setAnswerCount(post.getAnswerCount() + 1);
        postRepository.save(post);

        if (!post.getAuthor().getId().equals(author.getId())) {
            notificationService.createNotification(
                    post.getAuthor(), author,
                    com.techforum.enums.NotificationType.NEW_ANSWER,
                    "@" + author.getUsername() + " answered your question: "
                            + post.getTitle(),
                    "/posts/" + post.getId(),
                    post.getId(), "POST");
        }
        return answerMapper.toAnswerResponse(saved, List.of());
    }

    // ── UPDATE ANSWER ──────────────────────────────────────
    @Override
    @Transactional
    public AnswerResponse updateAnswer(Long answerId,
                                       UpdateAnswerRequest request,
                                       String username) {
        Answer answer = getActiveAnswer(answerId);
        User user     = getUser(username);

        if (!answer.getAuthor().getId().equals(user.getId())
                && !hasRole(user, "ROLE_ADMIN")) {
            throw new BadRequestException(
                    "You are not allowed to edit this answer");
        }
        answer.setContent(request.getContent());
        Answer updated = answerRepository.save(answer);
        return answerMapper.toAnswerResponse(updated,
                getCommentResponses(answerId));
    }

    // ── DELETE ANSWER ──────────────────────────────────────
    @Override
    @Transactional
    public void deleteAnswer(Long answerId, String username) {
        Answer answer = getActiveAnswer(answerId);
        User user     = getUser(username);

        boolean isAdminOrMod = hasRole(user, "ROLE_ADMIN")
                || hasRole(user, "ROLE_MODERATOR");

        if (!answer.getAuthor().getId().equals(user.getId()) && !isAdminOrMod) {
            throw new BadRequestException(
                    "You are not allowed to delete this answer");
        }

        answer.setIsDeleted(true);
        answerRepository.save(answer);

        Post post = answer.getPost();
        post.setAnswerCount(Math.max(0, post.getAnswerCount() - 1));
        if (Boolean.TRUE.equals(answer.getIsVerifiedAnswer())) {
            post.setHasVerifiedAnswer(false);
        }
        postRepository.save(post);
    }

    // ── GET ANSWERS BY POST ────────────────────────────────
    @Override
    public List<AnswerResponse> getAnswersByPost(Long postId, String username) {
        getActivePost(postId);
        List<Answer> answers =
                answerRepository.findByPostIdOrderByVerifiedAndVotes(postId);

        return answers.stream().map(answer -> {
            List<CommentResponse> comments = getCommentResponses(answer.getId());
            AnswerResponse response = answerMapper.toAnswerResponse(answer, comments);
            if (username != null) {
                userRepository.findByUsername(username).ifPresent(user ->
                    answerVoteRepository
                            .findByUserIdAndAnswerId(user.getId(), answer.getId())
                            .ifPresent(v -> response.setUserVote(v.getVoteType().name()))
                );
            }
            return response;
        }).collect(Collectors.toList());
    }

    // ── VOTE ANSWER ────────────────────────────────────────
    @Override
    @Transactional
    public AnswerResponse voteAnswer(Long answerId,
                                     String voteTypeStr,
                                     String username) {
        Answer answer = getActiveAnswer(answerId);
        User user     = getUser(username);

        if (answer.getAuthor().getId().equals(user.getId())) {
            throw new BadRequestException("You cannot vote on your own answer");
        }

        // FIX: Validate voteType string — previously an invalid value threw
        //      an uncaught IllegalArgumentException → 500 response.
        VoteType voteType;
        try {
            voteType = VoteType.valueOf(voteTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid vote type. Must be UPVOTE or DOWNVOTE");
        }

        Optional<AnswerVote> existingVote =
                answerVoteRepository.findByUserIdAndAnswerId(
                        user.getId(), answerId);

        boolean toggled = false; // FIX: track whether vote was removed

        if (existingVote.isPresent()) {
            AnswerVote vote = existingVote.get();
            if (vote.getVoteType() == voteType) {
                // Same vote again — toggle off (remove vote)
                answerVoteRepository.delete(vote);
                int adj = voteType == VoteType.UPVOTE ? -1 : 1;
                answer.setVoteCount(answer.getVoteCount() + adj);
                toggled = true; // FIX: vote was removed
            } else {
                // Switch direction
                vote.setVoteType(voteType);
                answerVoteRepository.save(vote);
                int adj = voteType == VoteType.UPVOTE ? 2 : -2;
                answer.setVoteCount(answer.getVoteCount() + adj);
            }
        } else {
            // New vote
            answerVoteRepository.save(AnswerVote.builder()
                    .user(user).answer(answer).voteType(voteType).build());
            int adj = voteType == VoteType.UPVOTE ? 1 : -1;
            answer.setVoteCount(answer.getVoteCount() + adj);

            // Reputation change for answer author
            User author   = answer.getAuthor();
            int repChange = voteType == VoteType.UPVOTE ? 10 : -2;
            author.setReputationPoints(
                    Math.max(0, author.getReputationPoints() + repChange));
            userRepository.save(author);
        }

        Answer updated = answerRepository.save(answer);
        List<CommentResponse> comments = getCommentResponses(answerId);
        AnswerResponse response = answerMapper.toAnswerResponse(updated, comments);

        // FIX: If the vote was toggled off, userVote must be null (not the
        //      type of the vote that was just removed). Previously this always
        //      called response.setUserVote(voteType.name()) — even after removal —
        //      so the frontend showed an active vote that no longer existed.
        if (!toggled) {
            response.setUserVote(voteType.name());
        }
        // toggled == true → userVote stays null (default from builder)

        return response;
    }

    // ── VERIFY ANSWER ──────────────────────────────────────
    @Override
    @Transactional
    public AnswerResponse verifyAnswer(Long answerId,
                                       String moderatorUsername) {
        Answer answer    = getActiveAnswer(answerId);
        User moderator   = getUser(moderatorUsername);

        if (!hasRole(moderator, "ROLE_MODERATOR")
                && !hasRole(moderator, "ROLE_ADMIN")) {
            throw new BadRequestException(
                    "Only moderators and admins can verify answers");
        }

        Post post = answer.getPost();

        // Remove verification from any previously verified answer
        answerRepository.findVerifiedAnswerByPostId(post.getId())
                .ifPresent(prev -> {
                    prev.setIsVerifiedAnswer(false);
                    prev.setVerifiedBy(null);
                    prev.setVerifiedAt(null);
                    answerRepository.save(prev);
                });

        answer.setIsVerifiedAnswer(true);
        answer.setVerifiedBy(moderator.getId());
        answer.setVerifiedAt(LocalDateTime.now());
        answerRepository.save(answer);

        post.setHasVerifiedAnswer(true);
        post.setStatus(PostStatus.SOLVED);
        postRepository.save(post);

        // Reward answer author +25 reputation
        User answerAuthor = answer.getAuthor();
        answerAuthor.setReputationPoints(
                answerAuthor.getReputationPoints() + 25);
        userRepository.save(answerAuthor);

        if (!answerAuthor.getId().equals(moderator.getId())) {
            notificationService.createNotification(
                    answerAuthor, moderator,
                    com.techforum.enums.NotificationType.ANSWER_VERIFIED,
                    "Your answer was marked as verified solution!",
                    "/posts/" + post.getId(),
                    answer.getId(), "ANSWER");
        }

        return answerMapper.toAnswerResponse(answer,
                getCommentResponses(answerId));
    }

    // ── UNVERIFY ANSWER ────────────────────────────────────
    @Override
    @Transactional
    public AnswerResponse unverifyAnswer(Long answerId,
                                         String moderatorUsername) {
        Answer answer  = getActiveAnswer(answerId);
        User moderator = getUser(moderatorUsername);

        if (!hasRole(moderator, "ROLE_MODERATOR")
                && !hasRole(moderator, "ROLE_ADMIN")) {
            throw new BadRequestException(
                    "Only moderators and admins can unverify answers");
        }

        answer.setIsVerifiedAnswer(false);
        answer.setVerifiedBy(null);
        answer.setVerifiedAt(null);
        answerRepository.save(answer);

        Post post = answer.getPost();
        post.setHasVerifiedAnswer(false);
        post.setStatus(PostStatus.OPEN);
        postRepository.save(post);

        // Deduct reputation
        User answerAuthor = answer.getAuthor();
        answerAuthor.setReputationPoints(
                Math.max(0, answerAuthor.getReputationPoints() - 25));
        userRepository.save(answerAuthor);

        return answerMapper.toAnswerResponse(answer,
                getCommentResponses(answerId));
    }

    // ── PRIVATE HELPERS ────────────────────────────────────
    private Post getActivePost(Long postId) {
        return postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found: " + postId));
    }

    private Answer getActiveAnswer(Long answerId) {
        return answerRepository.findById(answerId)
                .filter(a -> !a.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Answer not found: " + answerId));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals(roleName));
    }

    private List<CommentResponse> getCommentResponses(Long answerId) {
        return commentRepository
                .findTopLevelCommentsByAnswerId(answerId)
                .stream()
                .map(answerMapper::toCommentResponse)
                .collect(Collectors.toList());
    }
}
