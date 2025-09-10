package com.gdse.aad.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimResponseDTO {
    private Long id;
    private Long foundItemId;
    private Long guestId;
    private String guestName;
    private String message;
    private String proofImagePath;
    private String status;
    private LocalDateTime createdAt;
}
