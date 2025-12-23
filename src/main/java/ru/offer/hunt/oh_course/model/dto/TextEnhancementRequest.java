package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextEnhancementRequest {

    @NotBlank(message = "Текст для улучшения не может быть пустым")
    private String text;

    @NotNull(message = "Необходимо выбрать действие")
    private TextEnhancementAction action;

    public enum TextEnhancementAction {
        SIMPLIFY,      // Упростить язык
        ACADEMIC,      // Сделать более академичным
        GRAMMAR,       // Исправить грамматику
        EXPAND,        // Расширить мысль
        EXAMPLE        // Придумать пример
    }
}

