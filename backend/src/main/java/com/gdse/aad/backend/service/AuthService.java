package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.*;

public interface AuthService {
    String register(RegisterDTO dto);
    AuthResponseDTO authenticate(AuthDTO dto);

    String guestRegister(GuestRegisterDTO dto);
    AuthResponseDTO guestAuthenticate(AuthDTO dto);

    boolean emailExists(String email);

}
