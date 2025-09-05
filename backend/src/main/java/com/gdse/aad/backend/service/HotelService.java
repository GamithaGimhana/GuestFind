package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.HotelDTO;

import java.util.List;

public interface HotelService {
    HotelDTO createHotel(HotelDTO hotelDTO);
    List<HotelDTO> getAllHotels();
}
