package ru.offer.hunt.oh_course.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.offer.hunt.oh_course.model.entity.QuestionOption;

import java.util.List;
import java.util.UUID;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, UUID> {

    List<QuestionOption> findByQuestionIdOrderBySortOrderAsc(UUID questionId);

    List<QuestionOption> findByQuestionIdInOrderBySortOrderAsc(List<UUID> questionIds);
}
