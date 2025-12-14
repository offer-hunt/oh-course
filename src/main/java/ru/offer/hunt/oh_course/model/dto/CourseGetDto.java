package ru.offer.hunt.oh_course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CourseGetDto {

    private OldCourseDto courseDto;
    private List<LessonWithPagesDto> lessons;

}
