package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.dto.GuestDTO;
import com.gdse.aad.backend.dto.PasswordUpdateDTO;
import com.gdse.aad.backend.entity.Guest;
import com.gdse.aad.backend.exception.ResourceNotFoundException;
import com.gdse.aad.backend.repository.GuestRepository;
import com.gdse.aad.backend.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    // Admin management

    @Override
    public GuestDTO createGuest(GuestDTO dto) {
        Guest guest = modelMapper.map(dto, Guest.class);

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            guest.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        Guest saved = guestRepository.save(guest);
        return modelMapper.map(saved, GuestDTO.class);
    }

    @Override
    public GuestDTO getGuestById(Long id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + id));
        return modelMapper.map(guest, GuestDTO.class);
    }

    @Override
    public List<GuestDTO> getAllGuests() {
        return guestRepository.findAll()
                .stream()
                .map(g -> modelMapper.map(g, GuestDTO.class))
                .toList();
    }

    @Override
    public GuestDTO updateGuest(Long id, GuestDTO dto) {
        Guest existing = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + id));

        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        Guest updated = guestRepository.save(existing);
        return modelMapper.map(updated, GuestDTO.class);
    }

    @Override
    public void deleteGuest(Long id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + id));
        guestRepository.delete(guest);
    }

    // Guest self profile

    @Override
    public GuestDTO getProfileByEmail(String email) {
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with email: " + email));
        return modelMapper.map(guest, GuestDTO.class);
    }

    @Transactional
    @Override
    public GuestDTO updateProfile(String email, GuestDTO dto) {
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with email: " + email));

        guest.setName(dto.getName());
        guest.setPhone(dto.getPhone());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            guest.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        Guest updated = guestRepository.save(guest);
        return modelMapper.map(updated, GuestDTO.class);
    }

    @Override
    @Transactional
    public void updatePassword(String email, PasswordUpdateDTO dto) {
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with email: " + email));

        if (!passwordEncoder.matches(dto.getOldPassword(), guest.getPasswordHash())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        guest.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        guestRepository.save(guest);
    }
}
