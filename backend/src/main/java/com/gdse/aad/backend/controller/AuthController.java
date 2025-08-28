package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.*;
import com.gdse.aad.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    // --- Staff/Admin ---
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> register(@RequestBody RegisterDTO dto) {
        String msg = authService.register(dto);
        return ResponseEntity.ok(new ApiResponseDTO(200, msg, null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO> login(@RequestBody AuthDTO dto) {
        var data = authService.authenticate(dto);
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", data));
    }

    // --- Guests ---
    @PostMapping("/guest/register")
    public ResponseEntity<ApiResponseDTO> guestRegister(@RequestBody GuestRegisterDTO dto) {
        String msg = authService.guestRegister(dto);
        return ResponseEntity.ok(new ApiResponseDTO(200, msg, null));
    }

    @PostMapping("/guest/login")
    public ResponseEntity<ApiResponseDTO> guestLogin(@RequestBody GuestLoginDTO dto) {
        var data = authService.guestAuthenticate(dto);
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", data));
    }
}
