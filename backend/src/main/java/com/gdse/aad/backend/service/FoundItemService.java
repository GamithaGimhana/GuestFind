package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.FoundItemRequestDTO;
import com.gdse.aad.backend.dto.FoundItemResponseDTO;

import java.io.IOException;
import java.util.List;

public interface FoundItemService {
    FoundItemResponseDTO createFoundItem(FoundItemRequestDTO dto) throws IOException;
    List<FoundItemResponseDTO> getAllFoundItems();
    List<FoundItemResponseDTO> getUnclaimedFoundItems();
    void archiveFoundItem(Long id);

}
