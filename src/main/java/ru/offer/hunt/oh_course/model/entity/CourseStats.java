package ru.offer.hunt.oh_course.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "course", name = "course_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CourseStats {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "enrollments", nullable = false)
    private Integer enrollments;

    @Column(name = "avg_completion", nullable = false, precision = 5, scale = 2)
    private BigDecimal avgCompletion;

    @Column(name = "avg_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal avgRating;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
