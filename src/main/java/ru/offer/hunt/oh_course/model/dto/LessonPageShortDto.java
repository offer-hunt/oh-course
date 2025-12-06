package ru.offer.hunt.oh_course.model.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import ru.offer.hunt.oh_course.model.enums.PageType;

@Value
@Builder
public class LessonPageShortDto {
    UUID id;
    String title;
    PageType pageType;
    Integer sortOrder;
}
