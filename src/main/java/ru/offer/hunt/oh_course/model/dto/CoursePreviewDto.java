package ru.offer.hunt.oh_course.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Структура курса для предпросмотра (курс -> уроки -> страницы)")
public class CoursePreviewDto {
    private CourseDto course;
    private List<LessonPreviewDto> lessons;
}