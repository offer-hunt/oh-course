package ru.offer.hunt.oh_course.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Урок с вложенными страницами для превью")
public class LessonPreviewDto extends LessonDto {
    private List<LessonPageDto> pages;
}