package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.PageType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class LessonPageDto {
    private UUID id;
    private UUID lessonId;
    private String title;
    private PageType pageType;
    private Integer sortOrder;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
