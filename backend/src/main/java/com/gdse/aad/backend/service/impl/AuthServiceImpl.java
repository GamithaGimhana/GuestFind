package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.dto.*;
import com.gdse.aad.backend.entity.Guest;
import com.gdse.aad.backend.entity.Hotel;
import com.gdse.aad.backend.entity.HotelStaff;
import com.gdse.aad.backend.exception.ResourceNotFoundException;
import com.gdse.aad.backend.repository.GuestRepository;
import com.gdse.aad.backend.repository.HotelRepository;
import com.gdse.aad.backend.repository.HotelStaffRepository;
import com.gdse.aad.backend.service.AuthService;
import com.gdse.aad.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final HotelStaffRepository hotelStaffRepository;
    private final GuestRepository guestRepository;
    private final HotelRepository hotelRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponseDTO authenticate(AuthDTO authDTO) {
        var staff = hotelStaffRepository.findByEmail(authDTO.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(authDTO.getPassword(), staff.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(staff.getEmail(), staff.getRole().name());
        StaffProfileDTO staffProfileDTO = new StaffProfileDTO(staff.getStaffId(), staff.getName(), staff.getEmail(), staff.getRole().name(), staff.getHotel().getName());
        return new AuthResponseDTO(token, staffProfileDTO);
    }

    @Override
    public String register(RegisterDTO dto) {
        hotelStaffRepository.findByEmail(dto.getUsername()).ifPresent(u -> {
            throw new RuntimeException("Username already exists");
        });

        Hotel hotel = hotelRepository.findById(dto.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

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

    @Override
    public AuthResponseDTO guestAuthenticate(GuestLoginDTO authDTO) {
        var guest = guestRepository.findByEmail(authDTO.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Guest not found"));

        if (!passwordEncoder.matches(authDTO.getPassword(), guest.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(guest.getEmail(), "GUEST");
        GuestDTO guestDTO = new GuestDTO(guest.getGuestId(), guest.getName(), guest.getEmail(), guest.getPhone());
        return new AuthResponseDTO(token, guestDTO);
    }

    @Override
    public String guestRegister(GuestRegisterDTO dto) {
        guestRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new RuntimeException("Guest already exists");
        });

        Guest guest = Guest.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .build();

        guestRepository.save(guest);
        return "Guest registered successfully";
    }

}
