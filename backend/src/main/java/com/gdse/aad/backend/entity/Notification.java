package com.gdse.aad.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime sentDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Type type;

    public enum Type {
        EMAIL, SMS
    }
}
