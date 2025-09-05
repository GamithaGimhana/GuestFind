package com.gdse.aad.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime sentDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false)
    private boolean isRead = false;

    public enum Type {
        EMAIL, SMS
    }
}
