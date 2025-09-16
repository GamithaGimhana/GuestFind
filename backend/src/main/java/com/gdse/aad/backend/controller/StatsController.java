package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> getAdminStats() {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "OK", statsService.getAdminStats())
        );
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponseDTO> getStaffStats(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "OK", statsService.getStaffStats(email))
        );
    }

    @GetMapping("/guest")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<ApiResponseDTO> getGuestStats(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "OK", statsService.getGuestStats(email))
        );
    }
}
