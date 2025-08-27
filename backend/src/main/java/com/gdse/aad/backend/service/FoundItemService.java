package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.FoundItemRequestDTO;
import com.gdse.aad.backend.dto.FoundItemResponseDTO;

import java.util.List;

public interface FoundItemService {
    FoundItemResponseDTO createFoundItem(FoundItemRequestDTO dto);
    List<FoundItemResponseDTO> getAllFoundItems();
}
