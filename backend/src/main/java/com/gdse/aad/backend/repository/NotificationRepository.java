package com.gdse.aad.backend.repository;

import com.gdse.aad.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Collection<Object> findByGuest_GuestId(Long guestId);
}
