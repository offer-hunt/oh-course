package ru.offer.hunt.oh_course.model.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LessonOutlineDto {
    UUID id;
    String title;
    Integer orderIndex;
    Integer durationMin;
    boolean isDemo;
    boolean locked;
}
