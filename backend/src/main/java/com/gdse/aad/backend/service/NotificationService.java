package com.gdse.aad.backend.service;

import com.gdse.aad.backend.dto.NotificationResponseDTO;
import com.gdse.aad.backend.entity.Guest;

import java.util.List;

public interface NotificationService {
    void sendNotification(Guest guest, String message);
    List<NotificationResponseDTO> getNotificationsForGuest(Long guestId);
    void markAsRead(Long notificationId);

}
