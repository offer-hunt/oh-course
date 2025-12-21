package ru.offer.hunt.oh_course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LessonOutlineLiteDto {
    UUID id;
    String title;
    Integer orderIndex;
    Integer durationMin;
    boolean isDemo;
    boolean locked;
    List<LessonPageOutlineLiteDto> pages;
}
