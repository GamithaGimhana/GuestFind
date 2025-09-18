package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.dto.NotificationResponseDTO;
import com.gdse.aad.backend.entity.Guest;
import com.gdse.aad.backend.entity.Notification;
import com.gdse.aad.backend.exception.ResourceNotFoundException;
import com.gdse.aad.backend.repository.GuestRepository;
import com.gdse.aad.backend.repository.NotificationRepository;
import com.gdse.aad.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;
    private final JavaMailSender mailSender;

    @Override
    @Transactional
    public void sendNotification(Guest guest, String message) {
        // Save in DB
        Notification n = Notification.builder()
                .guest(guest)
                .message(message)
                .type(Notification.Type.EMAIL)
                .isRead(false)
                .build();
        notificationRepository.save(n);

        // Send email using Gmail SMTP
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(guest.getEmail());
        mail.setSubject("GuestFind Notification");
        mail.setText(message);
        mail.setFrom("gamitha.gimhana99@gmail.com");
        try {
            mailSender.send(mail);
            System.out.println("Notification email sent to: " + guest.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    @Override
    public List<NotificationResponseDTO> getNotificationsForGuest(Long guestId) {
        guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + guestId));

        return notificationRepository.findByGuest_GuestId(guestId)
                .stream()
                .map(n -> modelMapper.map(n, NotificationResponseDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationResponseDTO> getNotificationsForGuestEmail(String email) {
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with email: " + email));

        return notificationRepository.findByGuest_GuestId(guest.getGuestId())
                .stream()
                .map(n -> modelMapper.map(n, NotificationResponseDTO.class))
                .toList();
    }

    @Override
    public List<NotificationResponseDTO> getAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .map(n -> modelMapper.map(n, NotificationResponseDTO.class))
                .toList();
    }
}
