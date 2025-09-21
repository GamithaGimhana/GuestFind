package com.gdse.aad.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GuestDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String password;
    private String role;
}

