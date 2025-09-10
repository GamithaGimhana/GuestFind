package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.dto.GuestDTO;
import com.gdse.aad.backend.dto.PasswordUpdateDTO;
import com.gdse.aad.backend.service.GuestService;
import com.gdse.aad.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;
    private final JwtUtil jwtUtil;

    // Guest (self)

    @GetMapping("/profile")
    public ResponseEntity<ApiResponseDTO> getProfile(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractUsername(token.substring(7));
        GuestDTO profile = guestService.getProfileByEmail(email);
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponseDTO> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody GuestDTO dto
    ) {
        String email = jwtUtil.extractUsername(token.substring(7));
        GuestDTO updated = guestService.updateProfile(email, dto);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Profile Updated", updated));
    }

    @PutMapping("/me/password")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<ApiResponseDTO> updateMyPassword(
            @RequestBody PasswordUpdateDTO dto,
            Authentication authentication
    ) {
        String email = authentication.getName();
        guestService.updatePassword(email, dto);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Password updated successfully", null));
    }

    // Admin management

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> getAllGuests() {
        List<GuestDTO> guests = guestService.getAllGuests();
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", guests));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> getGuestById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", guestService.getGuestById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> updateGuest(@PathVariable Long id, @RequestBody GuestDTO dto) {
        return ResponseEntity.ok(new ApiResponseDTO(200, "Guest Updated", guestService.updateGuest(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> deleteGuest(@PathVariable Long id) {
        guestService.deleteGuest(id);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Guest Deleted", null));
    }
}
