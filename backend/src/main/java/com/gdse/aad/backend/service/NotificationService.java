package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.NotificationResponseDTO;
import com.gdse.aad.backend.entity.Guest;

import java.io.IOException;
import java.util.List;

public interface NotificationService {
    void sendNotification(Guest guest, String message) throws IOException;
    List<NotificationResponseDTO> getNotificationsForGuest(Long guestId);
    void markAsRead(Long notificationId);
    List<NotificationResponseDTO> getNotificationsForGuestEmail(String email);
    List<NotificationResponseDTO> getAllNotifications();

}
