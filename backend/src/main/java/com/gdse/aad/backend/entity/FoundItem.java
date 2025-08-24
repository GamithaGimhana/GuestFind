package com.gdse.aad.backend.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imagePath;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Status status = Status.UNCLAIMED;

    public enum Status {
        UNCLAIMED, MATCHED, ARCHIVED
    }

    private LocalDateTime foundDate = LocalDateTime.now();
}
