package com.gdse.aad.backend;

import com.gdse.aad.backend.dto.LostItemRequestDTO;
import com.gdse.aad.backend.dto.NotificationResponseDTO;
import com.gdse.aad.backend.entity.Hotel;
import com.gdse.aad.backend.entity.HotelStaff;
import com.gdse.aad.backend.entity.LostItem;
import com.gdse.aad.backend.entity.Notification;
import com.gdse.aad.backend.repository.HotelRepository;
import com.gdse.aad.backend.repository.HotelStaffRepository;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(LostItemRequestDTO.class, LostItem.class)
                .addMappings(mapper -> mapper.skip(LostItem::setLostId));

        modelMapper.typeMap(Notification.class, NotificationResponseDTO.class)
                .addMappings(mapper -> mapper.map(Notification::getNotificationId, NotificationResponseDTO::setId));

        return modelMapper;
    }

    @Bean
    CommandLineRunner seed(HotelRepository hotels, HotelStaffRepository staff, PasswordEncoder encoder) {
        return args -> {
            if (staff.count() == 0) {
                Hotel h = hotels.save(Hotel.builder()
                        .name("Default Hotel")
                        .address("N/A")
                        .phone("N/A")
                        .build());

                staff.save(HotelStaff.builder()
                        .name("Super Admin")
                        .email("admin@guestfind.com")
                        .passwordHash(encoder.encode("admin123"))
                        .role(HotelStaff.Role.ADMIN)
                        .hotel(h)
                        .build());
            }
        };
    }
}
