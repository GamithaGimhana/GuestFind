package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.dto.ClaimResponseDTO;
import com.gdse.aad.backend.entity.Claim;
import com.gdse.aad.backend.entity.FoundItem;
import com.gdse.aad.backend.entity.Guest;
import com.gdse.aad.backend.exception.ResourceNotFoundException;
import com.gdse.aad.backend.repository.ClaimRepository;
import com.gdse.aad.backend.repository.FoundItemRepository;
import com.gdse.aad.backend.repository.GuestRepository;
import com.gdse.aad.backend.service.ClaimService;
import com.gdse.aad.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final FoundItemRepository foundItemRepository;
    private final GuestRepository guestRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;

    private final String UPLOAD_DIR = "./uploads/claims/";

    @Override
    @Transactional
    public ClaimResponseDTO createClaim(Long foundItemId, String guestEmail, String message, MultipartFile proofImage) {
        Guest guest = guestRepository.findByEmail(guestEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));

        FoundItem foundItem = foundItemRepository.findById(foundItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found"));

        // save image if exists
        String imagePath = null;
        if (proofImage != null && !proofImage.isEmpty()) {
            try {
                File dir = new File(UPLOAD_DIR);
                if (!dir.exists()) dir.mkdirs();
                String fileName = UUID.randomUUID() + "_" + proofImage.getOriginalFilename();
                File dest = new File(dir, fileName);
                proofImage.transferTo(dest);
                imagePath = "/uploads/claims/" + fileName;
            } catch (IOException e) {
                throw new RuntimeException("Failed to save proof image", e);
            }
        }

        Claim claim = Claim.builder()
                .foundItem(foundItem)
                .guest(guest)
                .message(message)
                .proofImagePath(imagePath)
                .status(Claim.Status.PENDING)
                .build();

        Claim saved = claimRepository.save(claim);

        // notify admins/staff (simple)
        notificationService.sendNotification(guest, "Your claim has been submitted for item: " + foundItem.getTitle());

        ClaimResponseDTO dto = modelMapper.map(saved, ClaimResponseDTO.class);
        dto.setFoundItemId(foundItemId);
        dto.setGuestId(guest.getGuestId());
        dto.setGuestName(guest.getName());
        return dto;
    }

    @Override
    public List<ClaimResponseDTO> getAllClaims() {
        return claimRepository.findAll().stream()
                .map(c -> {
                    ClaimResponseDTO dto = modelMapper.map(c, ClaimResponseDTO.class);
                    dto.setFoundItemId(c.getFoundItem().getFoundId());
                    dto.setGuestId(c.getGuest().getGuestId());
                    dto.setGuestName(c.getGuest().getName());
                    return dto;
                }).toList();
    }

    @Override
    @Transactional
    public void replyToClaim(Long claimId, String replyMessage, boolean approve) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        claim.setStatus(approve ? Claim.Status.APPROVED : Claim.Status.REJECTED);
        claimRepository.save(claim);

        // notify guest
        notificationService.sendNotification(claim.getGuest(), "Your claim update: " + replyMessage);

        // if approved, mark found item as CLAIMED/UNCLAIMED-> matched etc (depends on your FoundItem.Status)
        if (approve) {
            FoundItem found = claim.getFoundItem();
            found.setStatus(FoundItem.Status.CLAIMED); // ensure you have CLAIMED enum or adjust
            foundItemRepository.save(found);
        }
    }
}
