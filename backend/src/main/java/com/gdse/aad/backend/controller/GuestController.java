package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.dto.GuestDTO;
import com.gdse.aad.backend.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @PostMapping
    public ResponseEntity<ApiResponseDTO> createGuest(@RequestBody GuestDTO dto) {
        return ResponseEntity.ok(new ApiResponseDTO(200, "Created", guestService.createGuest(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> getGuestById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", guestService.getGuestById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO> getAllGuests() {
        List<GuestDTO> guests = guestService.getAllGuests();
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", guests));
    }
}
