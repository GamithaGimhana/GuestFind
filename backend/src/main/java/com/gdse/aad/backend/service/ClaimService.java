package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.ClaimResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ClaimService {
    ClaimResponseDTO createClaim(Long foundItemId, String guestEmail, String message, MultipartFile proofImage) throws IOException;
    List<ClaimResponseDTO> getAllClaims();
    void replyToClaim(Long claimId, String replyMessage, boolean approve) throws IOException;
}
