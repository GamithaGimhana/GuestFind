package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.dto.HotelDTO;
import com.gdse.aad.backend.entity.Hotel;
import com.gdse.aad.backend.repository.HotelRepository;
import com.gdse.aad.backend.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    @Override
    public HotelDTO createHotel(HotelDTO dto) {
        Hotel hotel = modelMapper.map(dto, Hotel.class);
        return modelMapper.map(hotelRepository.save(hotel), HotelDTO.class);
    }

    @Override
    public List<HotelDTO> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(hotel -> new HotelDTO(
                        hotel.getHotelId(),
                        hotel.getName(),
                        hotel.getAddress(),
                        hotel.getPhone()
                ))
                .toList();
    }

    @Override
    public HotelDTO getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));
        return modelMapper.map(hotel, HotelDTO.class);
    }

    @Override
    public HotelDTO updateHotel(Long id, HotelDTO dto) {
        Hotel existing = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));

        existing.setName(dto.getName());
        existing.setAddress(dto.getAddress());
        existing.setPhone(dto.getPhone());

        Hotel updated = hotelRepository.save(existing);
        return modelMapper.map(updated, HotelDTO.class);
    }

    @Override
    public void deleteHotel(Long id) {
        Hotel existing = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));
        hotelRepository.delete(existing);
    }

}
