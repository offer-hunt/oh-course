package ru.offer.hunt.oh_course.model.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.id.CourseTagId;

@Entity
@Table(schema = "course", name = "course_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CourseTag {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private CourseTagId id;
}
