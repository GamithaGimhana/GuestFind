package com.gdse.aad.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lost_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LostItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lostId;

    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imagePath;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Status status = Status.PENDING;

    public enum Status {
        PENDING, MATCHED, CLOSED
    }

    private LocalDateTime reportedDate = LocalDateTime.now();
}
