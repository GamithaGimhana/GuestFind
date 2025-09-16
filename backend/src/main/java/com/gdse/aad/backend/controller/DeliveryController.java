package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.dto.DeliveryDTO;
import com.gdse.aad.backend.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GUEST')")
    public ResponseEntity<ApiResponseDTO> createDelivery(
            @RequestBody DeliveryDTO dto,
            Authentication authentication) {
        String email = authentication.getName();
        DeliveryDTO created = deliveryService.createDelivery(dto, email);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Created", created));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        DeliveryDTO updated = deliveryService.updateStatus(id, status);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Updated", updated));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> getAllDeliveries() {
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", deliveryService.getAllDeliveries()));
    }

    @GetMapping("/lost/{lostItemId}")
    @PreAuthorize("hasAnyRole('ADMIN','GUEST')")
    public ResponseEntity<ApiResponseDTO> getByLostItem(@PathVariable Long lostItemId) {
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", deliveryService.getDeliveryByLostItem(lostItemId)));
    }
}
