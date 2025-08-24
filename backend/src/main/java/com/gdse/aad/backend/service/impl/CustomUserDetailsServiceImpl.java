package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.entity.HotelStaff;
import com.gdse.aad.backend.repository.HotelStaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final HotelStaffRepository hotelStaffRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        HotelStaff staff = hotelStaffRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(
                staff.getEmail(),
                staff.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + staff.getRole().name()))
        );
    }
}
