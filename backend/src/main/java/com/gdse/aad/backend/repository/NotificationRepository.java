package com.gdse.aad.backend.repository;

import com.gdse.aad.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByGuest_GuestId(Long guestId);
}
