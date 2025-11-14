package ru.offer.hunt.oh_course.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.id.RatingId;

import java.time.OffsetDateTime;

@Entity
@Table(schema = "course", name = "course_ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Rating {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private RatingId id;

    @Column(name = "value", nullable = false)
    private int value;

    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
