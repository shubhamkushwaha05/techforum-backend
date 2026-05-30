package com.techforum.dto.response;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TagResponse {
    private Long id;
    private String name;
    private String description;
    private Integer usageCount;
}
