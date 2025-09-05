package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.dto.NotificationResponseDTO;
import com.gdse.aad.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Get all notifications for a guest
    @GetMapping("/guest/{guestId}")
    public ResponseEntity<ApiResponseDTO> getNotificationsForGuest(@PathVariable Long guestId) {
        List<NotificationResponseDTO> notifications = notificationService.getNotificationsForGuest(guestId);
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", notifications));
    }

    // Mark a notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponseDTO> markNotificationAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Marked as Read", null));
    }
}
