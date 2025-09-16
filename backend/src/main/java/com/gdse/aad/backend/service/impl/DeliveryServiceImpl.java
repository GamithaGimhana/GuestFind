package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.dto.DeliveryDTO;
import com.gdse.aad.backend.entity.Delivery;
import com.gdse.aad.backend.entity.LostItem;
import com.gdse.aad.backend.exception.ResourceNotFoundException;
import com.gdse.aad.backend.repository.DeliveryRepository;
import com.gdse.aad.backend.repository.LostItemRepository;
import com.gdse.aad.backend.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final LostItemRepository lostItemRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public DeliveryDTO createDelivery(DeliveryDTO dto, String requesterEmail) {
        LostItem lostItem = lostItemRepository.findById(dto.getLostItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Lost item not found"));

        // guests can only request delivery for their own lost items
        if (lostItem.getGuest() != null &&
                !lostItem.getGuest().getEmail().equals(requesterEmail)) {
            throw new RuntimeException("You can only request delivery for your own lost items");
        }

        Delivery.Method method = Delivery.Method.valueOf(dto.getMethod().toUpperCase());

        Delivery delivery = Delivery.builder()
                .lostItem(lostItem)
                .method(method)
                .address(method == Delivery.Method.DELIVERY ? dto.getAddress() : null)
                .status(Delivery.Status.PENDING)
                .build();

        Delivery saved = deliveryRepository.save(delivery);
        DeliveryDTO response = modelMapper.map(saved, DeliveryDTO.class);
        response.setLostItemId(lostItem.getLostId());
        response.setMethod(saved.getMethod().name());
        response.setStatus(saved.getStatus().name());
        return response;
    }

    @Override
    @Transactional
    public DeliveryDTO updateStatus(Long id, String status) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        delivery.setStatus(Delivery.Status.valueOf(status.toUpperCase()));
        Delivery updated = deliveryRepository.save(delivery);

        DeliveryDTO dto = modelMapper.map(updated, DeliveryDTO.class);
        dto.setLostItemId(updated.getLostItem().getLostId());
        dto.setMethod(updated.getMethod().name());
        dto.setStatus(updated.getStatus().name());
        return dto;
    }

    @Override
    public List<DeliveryDTO> getAllDeliveries() {
        return deliveryRepository.findAll()
                .stream()
                .map(d -> {
                    DeliveryDTO dto = modelMapper.map(d, DeliveryDTO.class);
                    dto.setLostItemId(d.getLostItem().getLostId());
                    dto.setMethod(d.getMethod().name());
                    dto.setStatus(d.getStatus().name());
                    return dto;
                })
                .toList();
    }

    @Override
    public DeliveryDTO getDeliveryByLostItem(Long lostItemId) {
        Delivery delivery = deliveryRepository.findByLostItem_LostId(lostItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for lost item " + lostItemId));

        DeliveryDTO dto = modelMapper.map(delivery, DeliveryDTO.class);
        dto.setLostItemId(delivery.getLostItem().getLostId());
        dto.setMethod(delivery.getMethod().name());
        dto.setStatus(delivery.getStatus().name());
        return dto;
    }
}
