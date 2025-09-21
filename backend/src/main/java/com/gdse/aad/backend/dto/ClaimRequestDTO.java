package com.gdse.aad.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimRequestDTO {
    private Long foundItemId;
    private String message;
    private String proofImagePath;
}
