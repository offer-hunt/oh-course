package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class CourseStatsDto {
    private UUID courseId;
    private Integer enrollments;
    private BigDecimal avgCompletion;
    private BigDecimal avgRating;
    private OffsetDateTime updatedAt;
}
