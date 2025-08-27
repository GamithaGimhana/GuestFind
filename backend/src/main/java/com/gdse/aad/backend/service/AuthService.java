package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.AuthDTO;
import com.gdse.aad.backend.dto.AuthResponseDTO;
import com.gdse.aad.backend.dto.RegisterDTO;

public interface AuthService {
    AuthResponseDTO authenticate(AuthDTO authDTO);
    String register(RegisterDTO registerDTO);
    AuthResponseDTO guestAuthenticate(AuthDTO authDTO);
    String guestRegister(RegisterDTO dto);
}
