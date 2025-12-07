package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.AccessType;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CourseDto {
    private UUID id;
    private UUID authorId;
    private String title;
    private String slug;
    private String description;
    private String coverUrl;
    private String language;
    private String level;
    private Integer estimatedDurationMin;
    private CourseStatus status;
    private AccessType accessType;
    private String inviteCode;
    private boolean requiresEntitlement;
    private Integer maxFreeEnrollments;
    private Integer version;
    private OffsetDateTime publishedAt;
    private OffsetDateTime archivedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<TagRefDto> tags;
    private Integer estimatedDurationHours;
    private Integer membersCount;
    private BigDecimal avgCompletion;
    private BigDecimal avgRating;
    private List<LessonDto> lessons;
}
