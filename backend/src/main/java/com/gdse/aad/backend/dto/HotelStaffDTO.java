package com.gdse.aad.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class HotelStaffDTO {
    private Long staffId;
    private String name;
    private String email;
    private String phone;
    private String password;
    private String role;
    private Long hotelId;
}
