package com.gdse.aad.backend.repository;

import com.gdse.aad.backend.entity.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {
    List<LostItem> findByStatus(LostItem.Status status);
    List<LostItem> findByGuest_GuestId(Long guestId);
    List<LostItem> findByArchivedFalse();
    List<LostItem> findByArchivedTrue();
    long countByStatus(LostItem.Status status);
    Long countByGuestEmail(String email);
    Long countByGuestEmailAndStatus(String email, LostItem.Status status);
}
