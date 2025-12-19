package ru.offer.hunt.oh_course.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.offer.hunt.oh_course.model.entity.LessonPage;

import java.util.List;
import java.util.UUID;

public interface LessonPageRepository extends JpaRepository<LessonPage, UUID> {

    boolean existsByLessonId(UUID id);

    List<LessonPage> findByLessonIdOrderBySortOrderAsc(UUID lessonId);
    List<LessonPage> findByLessonId(UUID id);

    void deleteAllByLessonId(UUID lessonId);
}
