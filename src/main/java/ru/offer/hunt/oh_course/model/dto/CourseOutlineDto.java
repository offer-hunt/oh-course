package ru.offer.hunt.oh_course.model.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CourseOutlineDto {
    UUID courseId;
    String slug;
    List<LessonOutlineDto> lessons;
}
