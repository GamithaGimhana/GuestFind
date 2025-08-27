package com.gdse.aad.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoundItemRequestDTO {
    private Long staffId;
    private String title;
    private String description;
    private String imagePath;
}