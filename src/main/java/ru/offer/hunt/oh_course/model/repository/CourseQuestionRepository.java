package ru.offer.hunt.oh_course.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.offer.hunt.oh_course.model.entity.CourseQuestion;

public interface CourseQuestionRepository extends JpaRepository<CourseQuestion, UUID> {
    List<CourseQuestion> findByPageIdOrderBySortOrderAsc(UUID pageId);
}
