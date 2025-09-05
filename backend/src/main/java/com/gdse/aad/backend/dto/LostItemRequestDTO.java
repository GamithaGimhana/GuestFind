package com.gdse.aad.backend.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LostItemRequestDTO {
    private Long guestId;
    private String title;
    private String description;
    private String imagePath;
    private LocalDate dateLost;
    private String locationLost;
}
