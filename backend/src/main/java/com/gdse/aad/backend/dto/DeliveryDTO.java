package com.gdse.aad.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDTO {
    private Long deliveryId;
    private Long lostItemId;
    private String method;   // DELIVERY or PICKUP
    private String address;  // only for DELIVERY
    private String status;
}
