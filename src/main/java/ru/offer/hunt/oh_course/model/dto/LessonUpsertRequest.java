package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonUpsertRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Integer orderIndex;

    private Integer durationMin;
}
