package com.gdse.aad.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LostItemResponseDTO {
    private Long lostId;
    private String guestName;
    private String title;
    private String description;
    private String imagePath;
    private String status;
    private LocalDateTime reportedDate;
}
