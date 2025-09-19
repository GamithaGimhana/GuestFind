package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.dto.ClaimResponseDTO;
import com.gdse.aad.backend.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO> createClaim(
            @RequestParam Long foundItemId, @RequestParam(required = false) String message, @RequestPart(required = false) MultipartFile proofImage,
            Authentication authentication) throws IOException {

        String email = authentication.getName();
        ClaimResponseDTO dto = claimService.createClaim(foundItemId, email, message, proofImage);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Created", dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponseDTO> getAllClaims() {
        List<ClaimResponseDTO> list = claimService.getAllClaims();
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", list));
    }

    @PutMapping("/{id}/reply")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponseDTO> replyToClaim(@PathVariable Long id, @RequestBody ReplyDTO reply) {
        claimService.replyToClaim(id, reply.getMessage(), reply.isApprove());
        return ResponseEntity.ok(new ApiResponseDTO(200, "Replied", null));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> approveClaim(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "Claim Approved", claimService.approveClaim(id))
        );
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> rejectClaim(@PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "Claim Rejected", claimService.rejectClaim(id, reason))
        );
    }


    // small DTO for reply
    public static class ReplyDTO {
        private String message;
        private boolean approve;
        // getters/setters
        public String getMessage(){return message;}
        public void setMessage(String m){this.message=m;}
        public boolean isApprove(){return approve;}
        public void setApprove(boolean a){this.approve=a;}
    }
}
