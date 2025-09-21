package com.gdse.aad.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LostItemResponseDTO {
    private Long lostId;
    private String title;
    private String description;
    private String imagePath;
    private String status;
    private String guestName;
    private String location;
    private LocalDateTime createdAt;
}
