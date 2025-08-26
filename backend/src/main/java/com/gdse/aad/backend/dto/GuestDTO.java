package com.gdse.aad.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestDTO {
    private Long guestId;
    private String name;
    private String email;
    private String phone;
}
