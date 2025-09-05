package com.gdse.aad.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffProfileDTO {
    private Long staffId;
    private String name;
    private String email;
    private String role;
    private String hotelName;
}
