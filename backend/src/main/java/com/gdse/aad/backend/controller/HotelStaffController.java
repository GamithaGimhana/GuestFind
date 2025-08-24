package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.dto.StaffProfileDTO;
import com.gdse.aad.backend.service.HotelStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class HotelStaffController {

    private final HotelStaffService hotelStaffService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponseDTO> getProfile() {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "OK", hotelStaffService.getLoggedInStaffProfile())
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> getAllStaff() {
        List<StaffProfileDTO> staffList = hotelStaffService.getAllStaff();
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "OK", staffList)
        );
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponseDTO> staffDashboard() {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "OK", "Welcome to the Staff Dashboard")
        );
    }

    @GetMapping("/common")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponseDTO> sharedEndpoint() {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "OK", "This is accessible by both Admin and Staff")
        );
    }

}
