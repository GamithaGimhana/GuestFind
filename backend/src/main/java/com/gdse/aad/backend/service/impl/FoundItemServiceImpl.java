package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.dto.FoundItemRequestDTO;
import com.gdse.aad.backend.dto.FoundItemResponseDTO;
import com.gdse.aad.backend.entity.FoundItem;
import com.gdse.aad.backend.entity.HotelStaff;
import com.gdse.aad.backend.entity.LostItem;
import com.gdse.aad.backend.entity.MatchRecord;
import com.gdse.aad.backend.exception.ResourceNotFoundException;
import com.gdse.aad.backend.repository.FoundItemRepository;
import com.gdse.aad.backend.repository.HotelStaffRepository;
import com.gdse.aad.backend.repository.LostItemRepository;
import com.gdse.aad.backend.repository.MatchRecordRepository;
import com.gdse.aad.backend.service.FoundItemService;
import com.gdse.aad.backend.service.NotificationService;
import com.gdse.aad.backend.util.ItemMatcher;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoundItemServiceImpl implements FoundItemService {

    private final FoundItemRepository foundItemRepository;
    private final LostItemRepository lostItemRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final HotelStaffRepository staffRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public FoundItemResponseDTO createFoundItem(FoundItemRequestDTO dto) {
        HotelStaff staff = staffRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        FoundItem foundItem = FoundItem.builder()
                .staff(staff)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .imagePath(dto.getImagePath())
                .status(FoundItem.Status.UNCLAIMED)
                .claimed(false)
                .archived(false)
                .build();

        FoundItem saved = foundItemRepository.save(foundItem);

        // Auto-match logic
        List<LostItem> lostItems = lostItemRepository.findByStatus(LostItem.Status.PENDING);
        for (LostItem lost : lostItems) {
            if (ItemMatcher.isMatch(lost, saved)) {
                MatchRecord record = MatchRecord.builder()
                        .lostItem(lost)
                        .foundItem(saved)
                        .build();
                matchRecordRepository.save(record);

                lost.setStatus(LostItem.Status.MATCHED);
                saved.setStatus(FoundItem.Status.MATCHED);

                lostItemRepository.save(lost);
                foundItemRepository.save(saved);

                notificationService.sendNotification(lost.getGuest(),
                        "We may have found your item: " + saved.getTitle());
            }
        }

        FoundItemResponseDTO dtoResp = modelMapper.map(saved, FoundItemResponseDTO.class);
        dtoResp.setStaffName(staff.getName());
        return dtoResp;
    }

    @Override
    public List<FoundItemResponseDTO> getAllFoundItems() {
        return foundItemRepository.findByArchivedFalse()
                .stream()
                .map(item -> {
                    FoundItemResponseDTO dto = modelMapper.map(item, FoundItemResponseDTO.class);
                    dto.setStaffName(item.getStaff().getName());
                    return dto;
                })
                .toList();
    }

    @Override
    public List<FoundItemResponseDTO> getUnclaimedFoundItems() {
        return foundItemRepository.findByClaimedFalse()
                .stream()
                .filter(item -> !item.isArchived())
                .map(item -> modelMapper.map(item, FoundItemResponseDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public void archiveFoundItem(Long id) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found with id: " + id));

        item.setArchived(true);
        item.setStatus(FoundItem.Status.ARCHIVED);
        foundItemRepository.save(item);
    }

    @Transactional
    @Override
    public FoundItemResponseDTO updateFoundItem(Long id, FoundItemRequestDTO dto) {
        FoundItem existing = foundItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found with id: " + id));

        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setImagePath(dto.getImagePath());
        // optionally: allow status/location updates too if you have them in DTO

        FoundItem updated = foundItemRepository.save(existing);

        FoundItemResponseDTO resp = modelMapper.map(updated, FoundItemResponseDTO.class);
        resp.setStaffName(updated.getStaff().getName());
        return resp;
    }

    @Transactional
    @Override
    public String matchItem(Long foundItemId, Long lostItemId) {
        FoundItem found = foundItemRepository.findById(foundItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found with id: " + foundItemId));
        LostItem lost = lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item not found with id: " + lostItemId));

        // Create match record
        MatchRecord record = MatchRecord.builder()
                .foundItem(found)
                .lostItem(lost)
                .build();
        matchRecordRepository.save(record);

        // Update statuses
        found.setStatus(FoundItem.Status.MATCHED);
        lost.setStatus(LostItem.Status.MATCHED);
        foundItemRepository.save(found);
        lostItemRepository.save(lost);

        // Notify guest
        notificationService.sendNotification(lost.getGuest(),
                "Good news! A staff member matched your lost item with: " + found.getTitle());

        return "Match successful";
    }

}
