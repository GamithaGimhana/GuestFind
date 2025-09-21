package com.gdse.aad.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoundItemResponseDTO {
    private Long foundId;
    private String title;
    private String description;
    private String imagePath;
    private String status;
    private LocalDateTime createdAt;
    private String location;
    private String staffName;
}
