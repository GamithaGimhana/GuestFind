package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.GuestDTO;
import java.util.List;

public interface GuestService {
    GuestDTO createGuest(GuestDTO dto);
    GuestDTO getGuestById(Long id);
    List<GuestDTO> getAllGuests();
    GuestDTO updateGuest(Long id, GuestDTO dto);
    void deleteGuest(Long id);
}
