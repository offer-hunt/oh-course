package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.CourseMemberRole;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class CourseMemberDto {
    private UUID courseId;
    private UUID userId;
    private CourseMemberRole role;
    private OffsetDateTime addedAt;
    private UUID addedBy;
}
