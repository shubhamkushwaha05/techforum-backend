package com.techforum.config;
import com.techforum.dto.response.AnswerResponse;
import com.techforum.dto.response.CommentResponse;
import com.techforum.entity.Answer;
import com.techforum.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
@Component
public class AnswerMapper {
    @Autowired private PostMapper postMapper;
    public AnswerResponse toAnswerResponse(Answer answer, List<CommentResponse> comments) {
        return AnswerResponse.builder()
                .id(answer.getId()).content(answer.getContent())
                .author(postMapper.toAuthorResponse(answer.getAuthor()))
                .postId(answer.getPost().getId()).voteCount(answer.getVoteCount())
                .isVerifiedAnswer(answer.getIsVerifiedAnswer())
                .verifiedBy(answer.getVerifiedBy()).verifiedAt(answer.getVerifiedAt())
                .moderationStatus(answer.getModerationStatus())
                .comments(comments != null ? comments : Collections.emptyList())
                .createdAt(answer.getCreatedAt()).updatedAt(answer.getUpdatedAt())
                .userVote(null).build();
    }
    public CommentResponse toCommentResponse(Comment comment) {
        List<CommentResponse> replies = comment.getReplies() == null
                ? Collections.emptyList()
                : comment.getReplies().stream()
                    .filter(r -> !r.getIsDeleted())
                    .map(this::toCommentResponse)
                    .collect(Collectors.toList());
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getIsDeleted() ? "[deleted]" : comment.getContent())
                .author(comment.getIsDeleted() ? null : postMapper.toAuthorResponse(comment.getAuthor()))
                .postId(comment.getPost() != null ? comment.getPost().getId() : null)
                .answerId(comment.getAnswer() != null ? comment.getAnswer().getId() : null)
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .replies(replies).voteCount(comment.getVoteCount())
                .mentionedUser(comment.getMentionedUser()).isDeleted(comment.getIsDeleted())
                .createdAt(comment.getCreatedAt()).updatedAt(comment.getUpdatedAt())
                .build();
    }
}
