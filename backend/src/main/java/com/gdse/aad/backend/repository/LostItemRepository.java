package com.gdse.aad.backend.repository;

import com.gdse.aad.backend.entity.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {
}
