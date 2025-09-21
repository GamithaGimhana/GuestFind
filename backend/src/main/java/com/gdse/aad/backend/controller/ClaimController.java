package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.dto.ClaimRequestDTO;
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

    @PostMapping
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<ApiResponseDTO> createClaim(@RequestBody ClaimRequestDTO request, Authentication authentication) {
        String email = authentication.getName();
        ClaimResponseDTO dto = claimService.createClaim(request.getFoundItemId(), email, request.getMessage(), request.getProofImagePath());
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

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponseDTO> getClaimById(@PathVariable Long id) {
        ClaimResponseDTO claim = claimService.getClaimById(id);
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", claim));
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
