package com.gdse.aad.backend.entity;

import jakarta.persistence.*;
import lombok.*;

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

    private LocalDateTime archivedDate = LocalDateTime.now();
}
