package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.FoundItemRequestDTO;
import com.gdse.aad.backend.dto.FoundItemResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FoundItemService {
    FoundItemResponseDTO createFoundItem(FoundItemRequestDTO dto);
    List<FoundItemResponseDTO> getAllFoundItems();
    List<FoundItemResponseDTO> getUnclaimedFoundItems();
    void archiveFoundItem(Long id);
    FoundItemResponseDTO updateFoundItem(Long id, FoundItemRequestDTO dto);
    String matchItem(Long foundItemId, Long lostItemId);
    List<FoundItemResponseDTO> getArchivedFoundItems();
    FoundItemResponseDTO getFoundItemById(Long id);
    void unarchiveFoundItem(Long id);
}
