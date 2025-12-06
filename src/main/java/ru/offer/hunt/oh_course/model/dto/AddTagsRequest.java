package ru.offer.hunt.oh_course.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Запрос на добавление тегов к курсу")
public class AddTagsRequest {
    @NotEmpty
    @Schema(description = "Список UUID тегов из справочника TagRef", example = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
    private List<UUID> tagIds;
}