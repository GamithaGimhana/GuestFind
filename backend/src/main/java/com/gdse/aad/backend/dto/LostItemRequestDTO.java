package com.gdse.aad.backend.dto;

import lombok.*;

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
}
