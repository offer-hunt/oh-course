package ru.offer.hunt.oh_course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CourseOutlineLiteDto {
    UUID courseId;
    String slug;
    List<LessonOutlineLiteDto> lessons;
}
