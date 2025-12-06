package ru.offer.hunt.oh_course.model.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;

public interface CourseRepository extends JpaRepository<Course, UUID>,
        JpaSpecificationExecutor<Course> {

    boolean existsBySlug(String slug);

    List<Course> findByAuthorId(UUID authorId);

    List<Course> findByStatus(CourseStatus status);

    Optional<Course> findBySlugAndStatus(String slug, CourseStatus status);
}
