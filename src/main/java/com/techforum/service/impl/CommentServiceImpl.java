package com.techforum.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techforum.config.AnswerMapper;
import com.techforum.dto.request.CreateCommentRequest;
import com.techforum.dto.request.UpdateCommentRequest;
import com.techforum.dto.response.CommentResponse;
import com.techforum.entity.Answer;
import com.techforum.entity.Comment;
import com.techforum.entity.Post;
import com.techforum.entity.User;
import com.techforum.exception.BadRequestException;
import com.techforum.exception.ResourceNotFoundException;
import com.techforum.repository.AnswerRepository;
import com.techforum.repository.CommentRepository;
import com.techforum.repository.PostRepository;
import com.techforum.repository.UserRepository;
import com.techforum.service.CommentService;
import com.techforum.service.NotificationService;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired private CommentRepository  commentRepository;
    @Autowired private PostRepository     postRepository;
    @Autowired private AnswerRepository   answerRepository;
    @Autowired private UserRepository     userRepository;
    @Autowired private AnswerMapper       answerMapper;
    @Autowired private NotificationService notificationService;

    // ── COMMENT ON POST ────────────────────────────────────
    @Override
    @Transactional
    public CommentResponse addCommentToPost(Long postId,
                                             CreateCommentRequest request,
                                             String username) {
        Post post = postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found: " + postId));

        User author = getUser(username);
        Comment parent = resolveParent(request.getParentId());

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(author)
                .post(post)
                .parent(parent)
                .mentionedUser(request.getMentionedUser())
                .voteCount(0)
                .isDeleted(false)
                .build();

        Comment saved = commentRepository.save(comment);

        // Notify post author (skip if commenting on own post)
        if (!post.getAuthor().getId().equals(author.getId())) {
            notificationService.createNotification(
                    post.getAuthor(), author,
                    com.techforum.enums.NotificationType.NEW_COMMENT,
                    "@" + author.getUsername() + " commented on your post",
                    "/posts/" + post.getId(),
                    post.getId(), "POST");
        }

        // Notify mentioned user
        if (request.getMentionedUser() != null
                && !request.getMentionedUser().isBlank()) {
            userRepository.findByUsername(request.getMentionedUser())
                    .ifPresent(mentioned -> {
                        // Don't notify if mentioned user is the commenter
                        if (!mentioned.getId().equals(author.getId())) {
                            notificationService.createNotification(
                                    mentioned, author,
                                    com.techforum.enums.NotificationType.MENTION,
                                    "@" + author.getUsername()
                                            + " mentioned you in a comment",
                                    "/posts/" + post.getId(),
                                    post.getId(), "POST");
                        }
                    });
        }

        return answerMapper.toCommentResponse(saved);
    }

    // ── COMMENT ON ANSWER ──────────────────────────────────
    @Override
    @Transactional
    public CommentResponse addCommentToAnswer(Long answerId,
                                              CreateCommentRequest request,
                                              String username) {
        Answer answer = answerRepository.findById(answerId)
                .filter(a -> !a.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Answer not found: " + answerId));

        User author = getUser(username);
        Comment parent = resolveParent(request.getParentId());

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(author)
                .answer(answer)
                .parent(parent)
                .mentionedUser(request.getMentionedUser())
                .voteCount(0)
                .isDeleted(false)
                .build();

        Comment saved = commentRepository.save(comment);

        // FIX: Notify answer author of new comment.
        //      Previously addCommentToAnswer() sent NO notification at all —
        //      answer authors never found out someone commented on their answer.
        if (!answer.getAuthor().getId().equals(author.getId())) {
            notificationService.createNotification(
                    answer.getAuthor(), author,
                    com.techforum.enums.NotificationType.NEW_COMMENT,
                    "@" + author.getUsername()
                            + " commented on your answer",
                    "/posts/" + answer.getPost().getId(),
                    answer.getId(), "ANSWER");
        }

        // Notify mentioned user
        if (request.getMentionedUser() != null
                && !request.getMentionedUser().isBlank()) {
            userRepository.findByUsername(request.getMentionedUser())
                    .ifPresent(mentioned -> {
                        if (!mentioned.getId().equals(author.getId())) {
                            notificationService.createNotification(
                                    mentioned, author,
                                    com.techforum.enums.NotificationType.MENTION,
                                    "@" + author.getUsername()
                                            + " mentioned you in a comment",
                                    "/posts/" + answer.getPost().getId(),
                                    answer.getId(), "ANSWER");
                        }
                    });
        }

        return answerMapper.toCommentResponse(saved);
    }

    // ── UPDATE COMMENT ─────────────────────────────────────
    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId,
                                          UpdateCommentRequest request,
                                          String username) {
        Comment comment = getActiveComment(commentId);
        User user = getUser(username);

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"));

        if (!comment.getAuthor().getId().equals(user.getId()) && !isAdmin) {
            throw new BadRequestException(
                    "You are not allowed to edit this comment");
        }
        comment.setContent(request.getContent());
        return answerMapper.toCommentResponse(commentRepository.save(comment));
    }

    // ── DELETE COMMENT ─────────────────────────────────────
    @Override
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = getActiveComment(commentId);
        User user = getUser(username);

        boolean isAdminOrMod = user.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN")
                        || r.getName().name().equals("ROLE_MODERATOR"));

        if (!comment.getAuthor().getId().equals(user.getId()) && !isAdminOrMod) {
            throw new BadRequestException(
                    "You are not allowed to delete this comment");
        }
        comment.setIsDeleted(true);
        commentRepository.save(comment);
    }

    // ── GET COMMENTS BY POST ───────────────────────────────
    @Override
    public List<CommentResponse> getCommentsByPost(Long postId) {
        return commentRepository.findTopLevelCommentsByPostId(postId)
                .stream()
                .map(answerMapper::toCommentResponse)
                .collect(Collectors.toList());
    }

    // ── GET COMMENTS BY ANSWER ─────────────────────────────
    @Override
    public List<CommentResponse> getCommentsByAnswer(Long answerId) {
        return commentRepository.findTopLevelCommentsByAnswerId(answerId)
                .stream()
                .map(answerMapper::toCommentResponse)
                .collect(Collectors.toList());
    }

    // ── GET REPLIES ────────────────────────────────────────
    @Override
    public List<CommentResponse> getReplies(Long parentCommentId) {
        return commentRepository.findRepliesByParentId(parentCommentId)
                .stream()
                .map(answerMapper::toCommentResponse)
                .collect(Collectors.toList());
    }

    // ── PRIVATE HELPERS ────────────────────────────────────
    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));
    }

    private Comment getActiveComment(Long commentId) {
        return commentRepository.findById(commentId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Comment not found: " + commentId));
    }

    private Comment resolveParent(Long parentId) {
        if (parentId == null) return null;
        return commentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parent comment not found: " + parentId));
    }
}
