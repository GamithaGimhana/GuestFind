package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.entity.Guest;
import com.gdse.aad.backend.entity.Notification;
import com.gdse.aad.backend.repository.NotificationRepository;
import com.gdse.aad.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
//    private final JavaMailSender mailSender;

    @Override
    @Transactional
    public void sendNotification(Guest guest, String message) {
        // Save in DB
        Notification n = Notification.builder()
                .guest(guest)
                .message(message)
                .type(Notification.Type.EMAIL)
                .build();
        notificationRepository.save(n);

        // Send email
//        SimpleMailMessage mail = new SimpleMailMessage();
//        mail.setTo(guest.getEmail());
//        mail.setSubject("GuestFind Notification");
//        mail.setText(message);
//        mailSender.send(mail);
    }
}
