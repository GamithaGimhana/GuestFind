package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.DeliveryDTO;

import java.util.List;

public interface DeliveryService {
    DeliveryDTO createDelivery(DeliveryDTO dto, String requesterEmail);
    DeliveryDTO updateStatus(Long id, String status);
    List<DeliveryDTO> getAllDeliveries();
    DeliveryDTO getDeliveryByLostItem(Long lostItemId);
    boolean existsByLostItemId(Long lostItemId);
}
