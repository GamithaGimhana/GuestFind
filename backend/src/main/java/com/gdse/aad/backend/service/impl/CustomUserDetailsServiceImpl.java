package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.entity.HotelStaff;
import com.gdse.aad.backend.repository.GuestRepository;
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
    private final GuestRepository guestRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Check HotelStaff
        return hotelStaffRepository.findByEmail(username)
                .map(staff -> new User(
                        staff.getEmail(),
                        staff.getPasswordHash(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + staff.getRole().name()))
                ))
                // 2. Check Guest
                .orElseGet(() -> guestRepository.findByEmail(username)
                        .map(guest -> new User(
                                guest.getEmail(),
                                guest.getPasswordHash(),
                                List.of(new SimpleGrantedAuthority("ROLE_GUEST"))
                        ))
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"))
                );
    }
}
