package ru.offer.hunt.oh_course.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.offer.hunt.oh_course.model.entity.CourseQuestionOption;

public interface CourseQuestionOptionRepository extends JpaRepository<CourseQuestionOption, UUID> {

    @Query("""
        select o from CourseQuestionOption o
        where o.question.id in :questionIds
        order by o.question.id, o.sortOrder
    """)
    List<CourseQuestionOption> findAllByQuestionIdsOrdered(@Param("questionIds") List<UUID> questionIds);
}
