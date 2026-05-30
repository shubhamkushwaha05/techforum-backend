package com.techforum.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommentResponse {
    private Long id;
    private String content;
    private AuthorResponse author;
    private Long postId;
    private Long answerId;
    private Long parentId;
    private List<CommentResponse> replies;
    private Integer voteCount;
    private String mentionedUser;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
