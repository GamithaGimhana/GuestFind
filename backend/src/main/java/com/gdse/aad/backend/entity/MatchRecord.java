package com.gdse.aad.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchId;

    @ManyToOne
    @JoinColumn(name = "lost_id", nullable = false)
    private LostItem lostItem;

    @ManyToOne
    @JoinColumn(name = "found_id", nullable = false)
    private FoundItem foundItem;

    private LocalDateTime matchDate = LocalDateTime.now();
}
