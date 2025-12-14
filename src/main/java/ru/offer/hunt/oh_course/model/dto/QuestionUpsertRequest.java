package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.QuestionType;

@Getter
@Setter
public class QuestionUpsertRequest {

    @NotNull(message = "Тип вопроса не должен быть пустым")
    private QuestionType type;

    @NotBlank(message = "Текст вопроса не должен быть пустым")
    private String text;

    private String correctAnswer;

    private Boolean useAiCheck;

    private Integer points;

    @NotNull(message = "Порядок вопроса не должен быть пустым")
    private Integer sortOrder;
}
