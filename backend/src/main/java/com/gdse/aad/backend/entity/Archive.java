package com.gdse.aad.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "archive")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Archive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long archiveId;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    public enum ItemType {
        LOST, FOUND
    }

    private Long itemId;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime archivedDate = LocalDateTime.now();
}
