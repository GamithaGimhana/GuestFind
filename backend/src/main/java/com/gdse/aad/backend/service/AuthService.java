package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.*;

public interface AuthService {
    String register(RegisterDTO dto);                    // staff/admin
    AuthResponseDTO authenticate(AuthDTO dto);           // staff/admin

    String guestRegister(GuestRegisterDTO dto);          // guest
    AuthResponseDTO guestAuthenticate(GuestLoginDTO dto);// guest
}
