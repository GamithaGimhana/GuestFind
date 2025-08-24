package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.dto.LostItemRequestDTO;
import com.gdse.aad.backend.service.LostItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lost-items")
@RequiredArgsConstructor
public class LostItemController {

    private final LostItemService lostItemService;

    @PostMapping
    public ResponseEntity<ApiResponseDTO> createLostItem(@RequestBody LostItemRequestDTO dto) {
        return ResponseEntity.ok(new ApiResponseDTO(200, "Created", lostItemService.createLostItem(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> getLostItemById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", lostItemService.getLostItemById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO> getAllLostItems() {
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", lostItemService.getAllLostItems()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> updateLostItem(@PathVariable Long id, @RequestBody LostItemRequestDTO dto) {
        return ResponseEntity.ok(new ApiResponseDTO(200, "Updated", lostItemService.updateLostItem(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> deleteLostItem(@PathVariable Long id) {
        lostItemService.deleteLostItem(id);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Deleted", null));
    }
}
