package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.CodeLanguage;

@Getter
@Setter
public class CodeTaskGenerationRequest {

    @NotBlank(message = "Тема задания не может быть пустой")
    private String topic;

    @NotNull(message = "Необходимо выбрать язык программирования")
    private CodeLanguage language;

    private String difficulty; // начальный, продвинутый

    private String requirements; // дополнительные требования
}

