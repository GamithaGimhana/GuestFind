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

    // Staff/Admin Authentication
    @Override
    public AuthResponseDTO authenticate(AuthDTO authDTO) {
        var staff = hotelStaffRepository.findByEmail(authDTO.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Staff not found with email: " + authDTO.getEmail()));

        if (!passwordEncoder.matches(authDTO.getPassword(), staff.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(staff.getEmail(), staff.getRole().name());

        HotelStaffDTO staffDTO = HotelStaffDTO.builder()
                .staffId(staff.getStaffId())
                .name(staff.getName())
                .email(staff.getEmail())
                .phone(staff.getPhone())
                .role(staff.getRole().name())
                .hotelId(staff.getHotel().getHotelId())
                .build();

        return new AuthResponseDTO(token, staffDTO);
    }

    @Override
    public String register(RegisterDTO dto) {
        hotelStaffRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new RuntimeException("Staff already exists with email: " + dto.getEmail());
        });

        Hotel hotel = hotelRepository.findById(dto.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        HotelStaff staff = HotelStaff.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(HotelStaff.Role.valueOf(dto.getRole()))
                .hotel(hotel)
                .build();

        hotelStaffRepository.save(staff);
        return "Staff registered successfully";
    }

    // Guest Authentication
    @Override
    public AuthResponseDTO guestAuthenticate(AuthDTO authDTO) {
        var guest = guestRepository.findByEmail(authDTO.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Guest not found with email: " + authDTO.getEmail()));

        if (!passwordEncoder.matches(authDTO.getPassword(), guest.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(guest.getEmail(), "GUEST");

        GuestDTO guestDTO = GuestDTO.builder()
                .id(guest.getGuestId())
                .name(guest.getName())
                .email(guest.getEmail())
                .phone(guest.getPhone())
                .role("GUEST")
                .build();

        return new AuthResponseDTO(token, guestDTO);
    }

    @Override
    public String guestRegister(GuestRegisterDTO dto) {
        guestRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new RuntimeException("Guest already exists with email: " + dto.getEmail());
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

    @Override
    public boolean emailExists(String email) {
        boolean staffExists = hotelStaffRepository.existsByEmailIgnoreCase(email);
        boolean guestExists = guestRepository.existsByEmailIgnoreCase(email);
        return staffExists || guestExists;
    }

}
