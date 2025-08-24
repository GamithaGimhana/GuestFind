package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.dto.AuthDTO;
import com.gdse.aad.backend.dto.AuthResponseDTO;
import com.gdse.aad.backend.dto.RegisterDTO;
import com.gdse.aad.backend.entity.Hotel;
import com.gdse.aad.backend.entity.HotelStaff;
import com.gdse.aad.backend.repository.HotelRepository;
import com.gdse.aad.backend.repository.HotelStaffRepository;
import com.gdse.aad.backend.service.AuthService;
import com.gdse.aad.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final HotelStaffRepository hotelStaffRepository;
    private final HotelRepository hotelRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponseDTO authenticate(AuthDTO authDTO) {
        var staff = hotelStaffRepository.findByEmail(authDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(authDTO.getPassword(), staff.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(staff.getEmail(), staff.getRole().name());
        return new AuthResponseDTO(token);
    }

    @Override
    public String register(RegisterDTO dto) {
        hotelStaffRepository.findByEmail(dto.getUsername()).ifPresent(u -> {
            throw new RuntimeException("Username already exists");
        });

        Hotel hotel = hotelRepository.findById(dto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        HotelStaff staff = HotelStaff.builder()
                .name(dto.getName())
                .email(dto.getUsername())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(HotelStaff.Role.valueOf(dto.getRole()))
                .hotel(hotel)
                .build();

        hotelStaffRepository.save(staff);
        return "User registered successfully";
    }
}
