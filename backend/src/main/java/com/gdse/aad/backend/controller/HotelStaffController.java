package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.dto.HotelStaffDTO;
import com.gdse.aad.backend.dto.PasswordUpdateDTO;
import com.gdse.aad.backend.service.HotelStaffService;
import com.gdse.aad.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class HotelStaffController {

    private final HotelStaffService hotelStaffService;
    private final JwtUtil jwtUtil;

    // Staff (self)

    @GetMapping("/profile")
    public ResponseEntity<ApiResponseDTO> getProfile(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractUsername(token.substring(7));
        HotelStaffDTO profile = hotelStaffService.getProfileByEmail(email);
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponseDTO> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody HotelStaffDTO dto
    ) {
        String email = jwtUtil.extractUsername(token.substring(7));
        HotelStaffDTO updated = hotelStaffService.updateProfile(email, dto);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Profile Updated", updated));
    }

    @PutMapping("/me/password")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponseDTO> updateMyPassword(
            @RequestBody PasswordUpdateDTO dto,
            Authentication authentication
    ) {
        String email = authentication.getName();
        hotelStaffService.updatePassword(email, dto);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Password updated successfully", null));
    }

    // Admin management

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> getAllStaff() {
        List<HotelStaffDTO> staffList = hotelStaffService.getAllStaff();
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", staffList));
    }

    @GetMapping("/admin/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> getStaffByEmail(@PathVariable String email) {
        HotelStaffDTO staff = hotelStaffService.getProfileByEmailFull(email);
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", staff));
    }

    @PutMapping("/admin/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> updateStaffByEmail(
            @PathVariable String email,
            @RequestBody HotelStaffDTO dto
    ) {
        HotelStaffDTO updated = hotelStaffService.adminUpdateProfile(email, dto);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Staff Updated", updated));
    }
}
