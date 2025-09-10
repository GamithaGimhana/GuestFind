package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.HotelStaffDTO;
import com.gdse.aad.backend.dto.PasswordUpdateDTO;

import java.util.List;

public interface HotelStaffService {
    HotelStaffDTO getLoggedInStaffProfile();
    List<HotelStaffDTO> getAllStaff();
    HotelStaffDTO getProfileByEmail(String email);
    HotelStaffDTO updateProfile(String email, HotelStaffDTO dto);
    HotelStaffDTO getProfileByEmailFull(String email);
    void updatePassword(String email, PasswordUpdateDTO dto);
    HotelStaffDTO adminUpdateProfile(String email, HotelStaffDTO dto);
}
