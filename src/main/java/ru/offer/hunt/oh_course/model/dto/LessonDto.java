package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class LessonDto {
    private UUID id;
    private UUID courseId;
    private String title;
    private String description;
    private Integer orderIndex;
    private Integer durationMin;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
