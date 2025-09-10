package com.gdse.aad.backend.controller;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import com.gdse.aad.backend.dto.NotificationResponseDTO;
import com.gdse.aad.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Guest gets only their own notifications (via JWT)
    @GetMapping("/me")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<ApiResponseDTO> getMyNotifications(Authentication authentication) {
        String email = authentication.getName(); // comes from JWT
        List<NotificationResponseDTO> notifications = notificationService.getNotificationsForGuestEmail(email);
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", notifications));
    }

    // Mark notification as read
    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<ApiResponseDTO> markNotificationAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(new ApiResponseDTO(200, "Marked as Read", null));
    }

    // Admin can see all notifications (system-wide)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> getAllNotifications() {
        List<NotificationResponseDTO> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(new ApiResponseDTO(200, "OK", notifications));
    }
}

