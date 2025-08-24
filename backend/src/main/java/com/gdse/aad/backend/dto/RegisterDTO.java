package com.gdse.aad.backend.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String name;
    private String username; // email
    private String password;
    private String role;     // "STAFF" or "ADMIN"
    private Long hotelId;    // which hotel this staff belongs to
}
