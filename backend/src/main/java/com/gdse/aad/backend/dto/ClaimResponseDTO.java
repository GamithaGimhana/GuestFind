package com.gdse.aad.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimResponseDTO {
    private Long claimId;          // instead of id
    private Long foundItemId;
    private String foundItemTitle;
    private String foundItemImage;
    private Long guestId;
    private String guestName;
    private String guestEmail;
    private String message;
    private String proofImagePath;
    private String status;
    private LocalDateTime createdAt;
}
