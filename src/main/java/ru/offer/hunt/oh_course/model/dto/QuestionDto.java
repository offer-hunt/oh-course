package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.QuestionType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class QuestionDto {
    private UUID id;
    private UUID pageId;
    private QuestionType type;
    private String text;
    private String correctAnswer;
    private boolean useAiCheck;
    private Integer points;
    private Integer sortOrder;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
