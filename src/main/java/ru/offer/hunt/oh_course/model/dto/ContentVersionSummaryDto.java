package ru.offer.hunt.oh_course.model.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.VersionScope;

@Getter
@Setter
public class ContentVersionSummaryDto {

    private UUID id;
    private VersionScope scope;
    private UUID courseId;
    private UUID lessonId;
    private UUID createdBy;
    private OffsetDateTime createdAt;
    private String comment;
}
