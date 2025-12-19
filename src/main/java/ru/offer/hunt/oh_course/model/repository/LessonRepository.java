package ru.offer.hunt.oh_course.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.offer.hunt.oh_course.model.entity.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByCourseId(UUID courseId);

    List<Lesson> findByCourseIdOrderByOrderIndexAsc(UUID courseId);
}
