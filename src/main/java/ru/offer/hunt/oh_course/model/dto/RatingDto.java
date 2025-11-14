package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class RatingDto {
    private UUID userId;
    private UUID courseId;
    private int value;
    private String comment;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
