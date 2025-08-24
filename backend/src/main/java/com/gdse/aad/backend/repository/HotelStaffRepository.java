package com.gdse.aad.backend.repository;

import com.gdse.aad.backend.entity.HotelStaff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotelStaffRepository extends JpaRepository<HotelStaff, Long> {
    Optional<HotelStaff> findByEmail(String email);
}
