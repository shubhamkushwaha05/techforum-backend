package com.techforum.dto.response;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthorResponse {
    private Long id;
    private String username;
    private String fullName;
    private String profileImage;
    private Integer reputationPoints;
}
