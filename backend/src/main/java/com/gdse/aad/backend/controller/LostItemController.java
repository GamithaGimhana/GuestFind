package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.dto.LostItemRequestDTO;
import com.gdse.aad.backend.service.LostItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/lost-items")
@RequiredArgsConstructor
public class LostItemController {

    private final LostItemService lostItemService;

    // Guests report lost items
    @PostMapping
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<ApiResponseDTO> createLostItem(@RequestBody LostItemRequestDTO dto) {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "Created", lostItemService.createLostItem(dto))
        );
    }

    // Guest can see only their own lost items
    @GetMapping("/me")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<ApiResponseDTO> getMyLostItems(Authentication authentication) {
        String email = authentication.getName(); // comes from JWT
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "OK", lostItemService.getLostItemsByGuest(email))
        );
    }

    // Staff & Admin can see ALL lost items
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponseDTO> getAllLostItems() {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "OK", lostItemService.getAllLostItems())
        );
    }

    // Guest & Staff can view single lost item
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GUEST','STAFF','ADMIN')")
    public ResponseEntity<ApiResponseDTO> getLostItemById(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "OK", lostItemService.getLostItemById(id))
        );
    }


    // Guest can update only their own lost items
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<ApiResponseDTO> updateLostItem(@PathVariable Long id, @RequestBody LostItemRequestDTO dto) {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "Updated", lostItemService.updateLostItem(id, dto))
        );
    }

    // Guest can delete their own lost item
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<ApiResponseDTO> deleteLostItem(@PathVariable Long id) {
        lostItemService.deleteLostItem(id);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Deleted", null));
    }

    // Staff can update status of lost item
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponseDTO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "Status Updated", lostItemService.updateStatus(id, status))
        );
    }

    // Admin can archive lost items
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> archiveLostItem(@PathVariable Long id) {
        lostItemService.archiveLostItem(id);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Archived", null));
    }

    @GetMapping("/archived")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponseDTO> getArchivedLostItems() {
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", lostItemService.getArchivedLostItems()));
    }

    @PutMapping("/{id}/unarchive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> unarchiveLostItem(@PathVariable Long id) {
        lostItemService.unarchiveLostItem(id);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Unarchived", null));
    }

}

