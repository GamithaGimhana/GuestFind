package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.StaffProfileDTO;

import java.util.List;

public interface HotelStaffService {
    StaffProfileDTO getLoggedInStaffProfile();
    List<StaffProfileDTO> getAllStaff();

}
