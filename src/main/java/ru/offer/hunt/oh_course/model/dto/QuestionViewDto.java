package ru.offer.hunt.oh_course.model.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import ru.offer.hunt.oh_course.model.enums.QuestionType;

@Value
@Builder
public class QuestionViewDto {
    UUID id;
    QuestionType type;
    String text;
    List<QuestionOptionViewDto> options; // для choice-вопросов (без is_correct!)
}
