package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.*;
import com.gdse.aad.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO> register(@RequestBody RegisterDTO dto) {
        String msg = authService.register(dto);
        return ResponseEntity.ok(new ApiResponseDTO(200, "User registered successfully", msg));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO> login(@RequestBody AuthDTO dto) {
        var data = authService.authenticate(dto);
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", data));
    }
}
