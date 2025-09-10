package com.gdse.aad.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {
    private Long id;
    private String message;
    private String type;
    private boolean isRead;
    private LocalDateTime sentDate;
}
