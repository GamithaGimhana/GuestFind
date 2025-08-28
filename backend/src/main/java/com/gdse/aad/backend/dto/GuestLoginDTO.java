package com.gdse.aad.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestLoginDTO {
    private String email;
    private String password;
}
