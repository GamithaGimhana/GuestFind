package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.dto.HotelDTO;
import com.gdse.aad.backend.dto.StaffProfileDTO;
import com.gdse.aad.backend.entity.Hotel;
import com.gdse.aad.backend.repository.HotelRepository;
import com.gdse.aad.backend.service.HotelService;
import com.gdse.aad.backend.service.HotelStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> createHotel(@RequestBody HotelDTO hotelDTO) {
        HotelDTO saved = hotelService.createHotel(hotelDTO);
        return ResponseEntity.ok(new ApiResponseDTO(
                200, "Hotel created successfully", saved)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> getAllHotels() {
        List<HotelDTO> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "OK", hotels)
        );
    }
}
