package com.techforum.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnswerResponse {
    private Long id;
    private String content;
    private AuthorResponse author;
    private Long postId;
    private Integer voteCount;
    private Boolean isVerifiedAnswer;
    private Long verifiedBy;
    private LocalDateTime verifiedAt;
    private String moderationStatus;
    private List<CommentResponse> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userVote;
}
