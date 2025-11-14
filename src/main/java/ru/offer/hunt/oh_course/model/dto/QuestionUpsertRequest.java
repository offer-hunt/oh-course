package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.QuestionType;

@Getter
@Setter
public class QuestionUpsertRequest {

    @NotNull
    private QuestionType type;

    @NotBlank
    private String text;

    private String correctAnswer;

    private Boolean useAiCheck;

    private Integer points;

    @NotNull
    private Integer sortOrder;
}
