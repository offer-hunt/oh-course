package ru.offer.hunt.oh_course.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    boolean existsBySlug(String slug);

    List<Course> findByAuthorId(UUID authorId);

    List<Course> findByStatus(CourseStatus status);

    List<Course> findAllByAuthorIdAndTitleAndStatus(UUID authorId, String title, CourseStatus status);
}
