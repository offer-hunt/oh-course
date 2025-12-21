package ru.offer.hunt.oh_course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.offer.hunt.oh_course.model.enums.QuestionType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class QuestionOutlineLiteDto {
    private UUID id;
    private QuestionType type;
    private Integer sortOrder;
}
