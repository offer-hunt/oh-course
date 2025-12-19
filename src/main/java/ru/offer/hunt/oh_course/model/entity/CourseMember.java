package ru.offer.hunt.oh_course.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.CourseMemberRole;
import ru.offer.hunt.oh_course.model.id.CourseMemberId;

import java.time.OffsetDateTime;


@Entity
@Table(schema = "course", name = "course_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CourseMember {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private CourseMemberId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private CourseMemberRole role;

    @Column(name = "added_at", nullable = false)
    private OffsetDateTime addedAt;

    @Column(name = "added_by")
    private java.util.UUID addedBy;
}