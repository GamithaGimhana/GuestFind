package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.dto.FoundItemRequestDTO;
import com.gdse.aad.backend.service.FoundItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/found-items")
@RequiredArgsConstructor
public class FoundItemController {

    private final FoundItemService foundItemService;

    // Guest, Staff, Admin can report found items
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','GUEST')")
    public ResponseEntity<ApiResponseDTO> createFoundItem(@RequestBody FoundItemRequestDTO dto) {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "Created", foundItemService.createFoundItem(dto))
        );
    }

    // see ALL found items
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','GUEST')")
    public ResponseEntity<ApiResponseDTO> getAllFoundItems() {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "OK", foundItemService.getAllFoundItems())
        );
    }

    // See only unclaimed found items
    @GetMapping("/unclaimed")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','GUEST')")
    public ResponseEntity<ApiResponseDTO> getUnclaimedFoundItems() {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "OK", foundItemService.getUnclaimedFoundItems())
        );
    }

    // Admin can archive found items
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> archiveFoundItem(@PathVariable Long id) {
        foundItemService.archiveFoundItem(id);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Archived", null));
    }

    // Staff or Admin can edit found item
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponseDTO> updateFoundItem(
            @PathVariable Long id, @RequestBody FoundItemRequestDTO dto) {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "Updated", foundItemService.updateFoundItem(id, dto))
        );
    }

    // Match a found item with a lost item
    @PostMapping("/matches")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponseDTO> matchItem(
            @RequestParam Long foundItemId,
            @RequestParam Long lostItemId) {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "Matched", foundItemService.matchItem(foundItemId, lostItemId))
        );
    }

}

