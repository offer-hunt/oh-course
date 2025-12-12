package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionOptionUpsertRequest {

    @NotBlank(message = "Поле для варианта ответа не должно быть пустым")
    private String label;

    private Boolean correct;

    @NotNull
    private Integer sortOrder;
}
