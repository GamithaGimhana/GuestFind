package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.HotelDTO;

import java.util.List;

public interface HotelService {
    HotelDTO createHotel(HotelDTO hotelDTO);
    List<HotelDTO> getAllHotels();
    HotelDTO getHotelById(Long id);
    HotelDTO updateHotel(Long id, HotelDTO dto);
    void deleteHotel(Long id);

}
