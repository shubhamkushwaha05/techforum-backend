package com.techforum.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private AuthorResponse author;
    private Set<TagResponse> tags;
    private String status;
    private Integer voteCount;
    private Integer viewCount;
    private Integer answerCount;
    private Boolean isPinned;
    private Boolean hasVerifiedAnswer;
    private String moderationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isBookmarked;
    private String userVote;
}
