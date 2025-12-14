package ru.offer.hunt.oh_course.model.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QuestionOptionViewDto {
    UUID id;
    String label;
    Integer sortOrder;
}
