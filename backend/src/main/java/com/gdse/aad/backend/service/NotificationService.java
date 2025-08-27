package com.gdse.aad.backend.service;

import com.gdse.aad.backend.entity.Guest;

public interface NotificationService {
    void sendNotification(Guest guest, String message);
}
