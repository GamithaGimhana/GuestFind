package com.gdse.aad.backend.repository;

import com.gdse.aad.backend.entity.FoundItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {
    List<FoundItem> findByStatus(FoundItem.Status status);
    List<FoundItem> findByClaimedFalse();
    List<FoundItem> findByArchivedFalse();
    List<FoundItem> findByArchivedTrue();
}
