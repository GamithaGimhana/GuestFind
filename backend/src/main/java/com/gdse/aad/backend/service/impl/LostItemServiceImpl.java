package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.dto.LostItemRequestDTO;
import com.gdse.aad.backend.dto.LostItemResponseDTO;
import com.gdse.aad.backend.entity.FoundItem;
import com.gdse.aad.backend.entity.Guest;
import com.gdse.aad.backend.entity.LostItem;
import com.gdse.aad.backend.entity.MatchRecord;
import com.gdse.aad.backend.exception.ResourceNotFoundException;
import com.gdse.aad.backend.repository.FoundItemRepository;
import com.gdse.aad.backend.repository.GuestRepository;
import com.gdse.aad.backend.repository.LostItemRepository;
import com.gdse.aad.backend.repository.MatchRecordRepository;
import com.gdse.aad.backend.service.LostItemService;
import com.gdse.aad.backend.service.NotificationService;
import com.gdse.aad.backend.util.ItemMatcher;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LostItemServiceImpl implements LostItemService {

    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

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
                .archived(false)
                .build();

        LostItem saved = lostItemRepository.save(lostItem);

        // Auto-match with unclaimed found items
        List<FoundItem> foundItems = foundItemRepository.findByStatus(FoundItem.Status.UNCLAIMED);
        for (FoundItem found : foundItems) {
            if (ItemMatcher.isMatch(saved, found)) {
                MatchRecord record = MatchRecord.builder()
                        .lostItem(saved)
                        .foundItem(found)
                        .build();
                matchRecordRepository.save(record);

                saved.setStatus(LostItem.Status.MATCHED);
                found.setStatus(FoundItem.Status.MATCHED);

                lostItemRepository.save(saved);
                foundItemRepository.save(found);

                notificationService.sendNotification(saved.getGuest(), "We may have found your item: " + found.getTitle());
            }
        }

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
        return lostItemRepository.findByArchivedFalse()
                .stream()
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
            throw new IllegalArgumentException("Invalid status. Allowed: PENDING, MATCHED, FOUND, ARCHIVED");
        }

        item.setStatus(newStatus);
        LostItem saved = lostItemRepository.save(item);

        LostItemResponseDTO dto = modelMapper.map(saved, LostItemResponseDTO.class);
        dto.setGuestName(saved.getGuest().getName());
        return dto;
    }

    @Override
    public List<LostItemResponseDTO> getLostItemsByGuest(String email) {
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with email: " + email));

        return lostItemRepository.findByGuest_GuestId(guest.getGuestId())
                .stream()
                .filter(item -> !item.isArchived())
                .map(item -> modelMapper.map(item, LostItemResponseDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public void archiveLostItem(Long id) {
        LostItem item = lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item not found with id: " + id));

        item.setArchived(true);
        item.setStatus(LostItem.Status.ARCHIVED);
        lostItemRepository.save(item);
    }
}

