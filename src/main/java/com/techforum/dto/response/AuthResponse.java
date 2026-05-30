package com.techforum.dto.response;
import lombok.*;
import java.util.Set;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
    private Integer reputationPoints;
}
