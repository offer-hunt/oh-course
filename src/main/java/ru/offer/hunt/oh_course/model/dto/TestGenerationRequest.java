package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.QuestionType;

@Getter
@Setter
public class TestGenerationRequest {

    private String topic;

    @NotNull(message = "Необходимо выбрать тип вопросов")
    private QuestionType questionType;

    @NotNull(message = "Необходимо указать количество вопросов")
    @Min(value = 1, message = "Количество вопросов должно быть не менее 1")
    private Integer questionCount;

    private String difficulty; // начальный, средний, продвинутый
}

