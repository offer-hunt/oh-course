package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    private String description;

    @NotNull
    @Min(1)
    private Integer orderIndex;

    @Min(1)
    private Integer durationMin;

    // если null — считаем false
    private Boolean demo;
}
