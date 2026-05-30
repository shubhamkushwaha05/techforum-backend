package com.techforum.config;
import com.techforum.dto.response.*;
import com.techforum.entity.*;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
@Component
public class PostMapper {
    public PostResponse toPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId()).title(post.getTitle()).content(post.getContent())
                .author(toAuthorResponse(post.getAuthor()))
                .tags(post.getTags().stream().map(this::toTagResponse).collect(Collectors.toSet()))
                .status(post.getStatus().name()).voteCount(post.getVoteCount())
                .viewCount(post.getViewCount()).answerCount(post.getAnswerCount())
                .isPinned(post.getIsPinned()).hasVerifiedAnswer(post.getHasVerifiedAnswer())
                .moderationStatus(post.getModerationStatus())
                .createdAt(post.getCreatedAt()).updatedAt(post.getUpdatedAt())
                .isBookmarked(false).userVote(null)
                .build();
    }
    public AuthorResponse toAuthorResponse(User user) {
        return AuthorResponse.builder()
                .id(user.getId()).username(user.getUsername())
                .fullName(user.getFullName()).profileImage(user.getProfileImage())
                .reputationPoints(user.getReputationPoints()).build();
    }
    public TagResponse toTagResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId()).name(tag.getName())
                .description(tag.getDescription()).usageCount(tag.getUsageCount()).build();
    }
}
