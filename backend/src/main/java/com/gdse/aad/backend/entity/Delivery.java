package com.gdse.aad.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deliveryId;

    @ManyToOne
    @JoinColumn(name = "lost_id", nullable = false)
    private LostItem lostItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Method method;   // DELIVERY or PICKUP

    @Column(columnDefinition = "TEXT")
    private String address;  // required only if method == DELIVERY

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime deliveryDate;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    public enum Status {
        PENDING, SHIPPED, DELIVERED
    }

    public enum Method {
        DELIVERY, PICKUP
    }
}
