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

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final FoundItemRepository foundItemRepository;
    private final GuestRepository guestRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;

    private final String UPLOAD_DIR = "./uploads/claims/";

    @Transactional
    @Override
    public ClaimResponseDTO createClaim(Long foundItemId, String guestEmail, String message, String proofImageUrl) {
        Guest guest = guestRepository.findByEmail(guestEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));

        FoundItem foundItem = foundItemRepository.findById(foundItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found"));

        if (foundItem.isClaimed()) {
            throw new IllegalStateException("This item has already been claimed.");
        }

        Claim claim = Claim.builder()
                .foundItem(foundItem)
                .guest(guest)
                .message(message)
                .proofImagePath(proofImageUrl)
                .status(Claim.Status.PENDING)
                .build();

        Claim saved = claimRepository.save(claim);

        notificationService.sendNotification(guest, "Your claim for item '" + foundItem.getTitle() + "' has been submitted!");

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

        // if approved, mark found item as CLAIMED/UNCLAIMED-> matched
        if (approve) {
            FoundItem found = claim.getFoundItem();
            found.setStatus(FoundItem.Status.CLAIMED); // ensure have CLAIMED enum or adjust
            foundItemRepository.save(found);
        }
    }

    @Override
    @Transactional
    public ClaimResponseDTO approveClaim(Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        if (claim.getStatus() != Claim.Status.PENDING) {
            throw new IllegalStateException("Only pending claims can be approved");
        }

        claim.setStatus(Claim.Status.APPROVED);
        claimRepository.save(claim);

        // Update found item
        FoundItem foundItem = claim.getFoundItem();
        foundItem.setClaimed(true);
        foundItem.setStatus(FoundItem.Status.CLAIMED);
        foundItem.setClaimedBy(claim.getGuest()); // link to guest
        foundItemRepository.save(foundItem);

        // Notify guest
        notificationService.sendNotification(
                claim.getGuest(),
                "Your claim for the item '" + foundItem.getTitle() + "' has been approved!"
        );

        return modelMapper.map(claim, ClaimResponseDTO.class);
    }

    @Override
    @Transactional
    public ClaimResponseDTO rejectClaim(Long claimId, String reason) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        if (claim.getStatus() != Claim.Status.PENDING) {
            throw new IllegalStateException("Only pending claims can be rejected");
        }

        claim.setStatus(Claim.Status.REJECTED);
        claimRepository.save(claim);

        // Reset the found item so it can be claimed again
        FoundItem foundItem = claim.getFoundItem();
        foundItem.setClaimed(false);
        foundItem.setStatus(FoundItem.Status.UNCLAIMED);
        foundItem.setClaimedBy(null);
        foundItemRepository.save(foundItem);

        // Notify guest
        notificationService.sendNotification(
                claim.getGuest(),
                "Your claim for the item '" + foundItem.getTitle() + "' has been rejected. Reason: " + reason
        );

        return modelMapper.map(claim, ClaimResponseDTO.class);
    }

    @Override
    public ClaimResponseDTO getClaimById(Long id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + id));

        ClaimResponseDTO dto = modelMapper.map(claim, ClaimResponseDTO.class);
        dto.setFoundItemId(claim.getFoundItem().getFoundId());
        dto.setFoundItemTitle(claim.getFoundItem().getTitle());
        dto.setFoundItemImage(claim.getFoundItem().getImagePath());
        dto.setGuestId(claim.getGuest().getGuestId());
        dto.setGuestName(claim.getGuest().getName());
        dto.setGuestEmail(claim.getGuest().getEmail());

        return dto;
    }

}
