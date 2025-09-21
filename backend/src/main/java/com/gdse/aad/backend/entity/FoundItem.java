package com.gdse.aad.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "found_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foundId;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private HotelStaff staff;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imagePath;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(length = 255)
    private String location;

    @Column(nullable = false)
    private boolean claimed = false;

    @Column(nullable = false)
    private boolean archived = false;

    public enum Status {
        UNCLAIMED, CLAIMED, MATCHED, ARCHIVED
    }
}
