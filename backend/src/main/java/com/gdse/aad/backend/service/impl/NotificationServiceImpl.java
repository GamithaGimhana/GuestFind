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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;
//    private final JavaMailSender mailSender;

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

        // Send email (optional)
//        SimpleMailMessage mail = new SimpleMailMessage();
//        mail.setTo(guest.getEmail());
//        mail.setSubject("GuestFind Notification");
//        mail.setText(message);
//        mailSender.send(mail);
    }

//    @Override
//    public List<NotificationResponseDTO> getNotificationsForGuest(Long guestId) {
//        List<Notification> notifications = notificationRepository.findByGuest_GuestId(guestId);
//        return notifications.stream()
//                .map(n -> modelMapper.map(n, NotificationResponseDTO.class))
//                .toList();
//    }
    @Override
    public List<NotificationResponseDTO> getNotificationsForGuest(Long guestId) {
        guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + guestId));

        return notificationRepository.findByGuest_GuestId(guestId)
                .stream()
                .map(n -> modelMapper.map(n, NotificationResponseDTO.class))  // modelMapper handles it now
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
}
