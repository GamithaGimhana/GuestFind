package com.gdse.aad.backend.repository;

import com.gdse.aad.backend.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByLostItem_LostId(Long lostId);
    boolean existsByLostItem_LostId(Long lostItemId);
}
