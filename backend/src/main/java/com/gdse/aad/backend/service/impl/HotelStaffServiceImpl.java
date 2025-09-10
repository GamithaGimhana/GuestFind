package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.dto.HotelStaffDTO;
import com.gdse.aad.backend.dto.PasswordUpdateDTO;
import com.gdse.aad.backend.entity.Hotel;
import com.gdse.aad.backend.entity.HotelStaff;
import com.gdse.aad.backend.exception.ResourceNotFoundException;
import com.gdse.aad.backend.repository.HotelRepository;
import com.gdse.aad.backend.repository.HotelStaffRepository;
import com.gdse.aad.backend.service.HotelStaffService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelStaffServiceImpl implements HotelStaffService {

    private final HotelStaffRepository hotelStaffRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public HotelStaffDTO getLoggedInStaffProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return getProfileByEmail(email);
    }

    @Override
    public List<HotelStaffDTO> getAllStaff() {
        return hotelStaffRepository.findAll().stream()
                .map(staff -> modelMapper.map(staff, HotelStaffDTO.class))
                .toList();
    }

    @Override
    public HotelStaffDTO getProfileByEmail(String email) {
        HotelStaff staff = hotelStaffRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with email: " + email));
        return modelMapper.map(staff, HotelStaffDTO.class);
    }

    @Override
    @Transactional
    public HotelStaffDTO updateProfile(String email, HotelStaffDTO dto) {
        HotelStaff staff = hotelStaffRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with email: " + email));

        staff.setName(dto.getName());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            staff.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        // role/hotel cannot be changed by staff itself
        HotelStaff updated = hotelStaffRepository.save(staff);
        return modelMapper.map(updated, HotelStaffDTO.class);
    }

    @Override
    public HotelStaffDTO getProfileByEmailFull(String email) {
        HotelStaff staff = hotelStaffRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with email: " + email));
        return modelMapper.map(staff, HotelStaffDTO.class);
    }

    @Override
    @Transactional
    public void updatePassword(String email, PasswordUpdateDTO dto) {
        HotelStaff staff = hotelStaffRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with email: " + email));

        if (!passwordEncoder.matches(dto.getOldPassword(), staff.getPasswordHash())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        staff.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        hotelStaffRepository.save(staff);
    }

    @Override
    @Transactional
    public HotelStaffDTO adminUpdateProfile(String email, HotelStaffDTO dto) {
        HotelStaff staff = hotelStaffRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with email: " + email));

        staff.setName(dto.getName());
        staff.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            staff.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getRole() != null) {
            staff.setRole(HotelStaff.Role.valueOf(dto.getRole().toUpperCase()));
        }

        if (dto.getHotelId() != null) {
            Hotel hotel = hotelRepository.findById(dto.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + dto.getHotelId()));
            staff.setHotel(hotel);
        }

        HotelStaff updated = hotelStaffRepository.save(staff);
        return modelMapper.map(updated, HotelStaffDTO.class);
    }
}

