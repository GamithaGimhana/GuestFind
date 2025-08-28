package com.gdse.aad.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestRegisterDTO {
    private String name;
    private String email;
    private String phone;
    private String password;
}
