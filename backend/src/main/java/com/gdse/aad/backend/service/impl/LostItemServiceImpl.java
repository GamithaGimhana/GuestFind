package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.dto.LostItemRequestDTO;
import com.gdse.aad.backend.dto.LostItemResponseDTO;
import com.gdse.aad.backend.entity.Guest;
import com.gdse.aad.backend.entity.LostItem;
import com.gdse.aad.backend.exception.ResourceNotFoundException;
import com.gdse.aad.backend.repository.GuestRepository;
import com.gdse.aad.backend.repository.LostItemRepository;
import com.gdse.aad.backend.service.LostItemService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LostItemServiceImpl implements LostItemService {

    private final LostItemRepository lostItemRepository;
    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public LostItemResponseDTO createLostItem(LostItemRequestDTO requestDTO) {
        Guest guest = guestRepository.findById(requestDTO.getGuestId())
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));

        LostItem lostItem = LostItem.builder()
                .guest(guest)
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .imagePath(requestDTO.getImagePath())
                .status(LostItem.Status.PENDING)
                .build();

        LostItem saved = lostItemRepository.save(lostItem);

        // Ensure guestName is set in the response
        LostItemResponseDTO dto = modelMapper.map(saved, LostItemResponseDTO.class);
        dto.setGuestName(saved.getGuest().getName());
        return dto;
    }

    @Override
    public LostItemResponseDTO getLostItemById(Long id) {
        LostItem lostItem = lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item not found"));

        LostItemResponseDTO dto = modelMapper.map(lostItem, LostItemResponseDTO.class);
        dto.setGuestName(lostItem.getGuest().getName());
        return dto;
    }

    @Override
    public List<LostItemResponseDTO> getAllLostItems() {
        List<LostItem> items = lostItemRepository.findAll();
        return items.stream()
                .map(item -> {
                    LostItemResponseDTO dto = modelMapper.map(item, LostItemResponseDTO.class);
                    dto.setGuestName(item.getGuest().getName());
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public LostItemResponseDTO updateLostItem(Long id, LostItemRequestDTO requestDTO) {
        LostItem existing = lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item not found"));

        existing.setTitle(requestDTO.getTitle());
        existing.setDescription(requestDTO.getDescription());
        existing.setImagePath(requestDTO.getImagePath());

        LostItem updated = lostItemRepository.save(existing);

        LostItemResponseDTO dto = modelMapper.map(updated, LostItemResponseDTO.class);
        dto.setGuestName(updated.getGuest().getName());
        return dto;
    }

    @Override
    @Transactional
    public void deleteLostItem(Long id) {
        LostItem existing = lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item not found"));
        lostItemRepository.delete(existing);
    }

    @Override
    @Transactional
    public LostItemResponseDTO updateStatus(Long id, String status) {
        LostItem item = lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item not found with id: " + id));

        LostItem.Status newStatus;
        try {
            newStatus = LostItem.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status. Allowed: PENDING, MATCHED, CLOSED");
        }

        item.setStatus(newStatus);
        LostItem saved = lostItemRepository.save(item);

        LostItemResponseDTO dto = modelMapper.map(saved, LostItemResponseDTO.class);
        dto.setGuestName(saved.getGuest().getName());
        return dto;
    }
}
