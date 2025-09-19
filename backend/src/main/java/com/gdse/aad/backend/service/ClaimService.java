package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.ClaimResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ClaimService {
    ClaimResponseDTO createClaim(Long foundItemId, String guestEmail, String message, MultipartFile proofImage);
    List<ClaimResponseDTO> getAllClaims();
    void replyToClaim(Long claimId, String replyMessage, boolean approve);
    ClaimResponseDTO approveClaim(Long claimId);
    ClaimResponseDTO rejectClaim(Long claimId, String reason);
}
