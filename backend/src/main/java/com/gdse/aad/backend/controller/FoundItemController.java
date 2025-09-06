package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.dto.FoundItemRequestDTO;
import com.gdse.aad.backend.service.FoundItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/found-items")
@RequiredArgsConstructor
public class FoundItemController {

    private final FoundItemService foundItemService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponseDTO> createFoundItem(@RequestBody FoundItemRequestDTO dto) {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "Created", foundItemService.createFoundItem(dto))
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','Guest')")
    public ResponseEntity<ApiResponseDTO> getAllFoundItems() {
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "OK", foundItemService.getAllFoundItems())
        );
    }
}
