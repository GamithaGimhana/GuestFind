package com.gdse.aad.backend.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String role;     // "STAFF" or "ADMIN"
    private Long hotelId;

}
