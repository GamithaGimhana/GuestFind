package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.GuestDTO;
import com.gdse.aad.backend.dto.PasswordUpdateDTO;

import java.util.List;

public interface GuestService {
    GuestDTO createGuest(GuestDTO dto);
    GuestDTO getGuestById(Long id);
    List<GuestDTO> getAllGuests();
    GuestDTO updateGuest(Long id, GuestDTO dto);
    void deleteGuest(Long id);
    GuestDTO getProfileByEmail(String email);
    GuestDTO updateProfile(String email, GuestDTO dto);
    void updatePassword(String email, PasswordUpdateDTO dto);
}
