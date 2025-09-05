package com.gdse.aad.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // prevent null user/admin fields from being in json
public class AuthResponseDTO {
    private String token;
    private Object user; // Can be GuestDTO or StaffProfileDTO

    public AuthResponseDTO(String token) {
        this.token = token;
        this.user = null;
    }
}
