package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.LostItemRequestDTO;
import com.gdse.aad.backend.dto.LostItemResponseDTO;

import java.util.List;

public interface LostItemService {
    LostItemResponseDTO createLostItem(LostItemRequestDTO requestDTO);
    LostItemResponseDTO getLostItemById(Long id);
    List<LostItemResponseDTO> getAllLostItems();
    LostItemResponseDTO updateLostItem(Long id, LostItemRequestDTO requestDTO);
    void deleteLostItem(Long id);
    LostItemResponseDTO updateStatus(Long id, String status);
    List<LostItemResponseDTO> getLostItemsByGuest(String email);
    void archiveLostItem(Long id);

}
