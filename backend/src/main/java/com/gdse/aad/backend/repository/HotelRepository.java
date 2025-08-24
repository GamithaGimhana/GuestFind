package com.gdse.aad.backend.repository;

import com.gdse.aad.backend.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
