package ru.offer.hunt.oh_course.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.offer.hunt.oh_course.model.entity.CourseTag;
import ru.offer.hunt.oh_course.model.id.CourseTagId;

import java.util.List;
import java.util.UUID;

public interface CourseTagRepository extends JpaRepository<CourseTag, CourseTagId> {

    List<CourseTag> findByIdCourseId(UUID courseId);

    List<CourseTag> findByIdTagId(UUID tagId);
}
