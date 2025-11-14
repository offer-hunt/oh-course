package ru.offer.hunt.oh_course.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.AccessType;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "course", name = "course_courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Course {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "cover_url", length = 512)
    private String coverUrl;

    @Column(name = "language", length = 16)
    private String language;

    @Column(name = "level", length = 32)
    private String level;

    @Column(name = "estimated_duration_min")
    private Integer estimatedDurationMin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CourseStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false)
    private AccessType accessType;

    @Column(name = "invite_code", length = 64)
    private String inviteCode;

    @Column(name = "requires_entitlement", nullable = false)
    private boolean requiresEntitlement;

    @Column(name = "max_free_enrollments")
    private Integer maxFreeEnrollments;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
