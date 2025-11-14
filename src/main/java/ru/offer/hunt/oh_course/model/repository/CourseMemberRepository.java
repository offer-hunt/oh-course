package ru.offer.hunt.oh_course.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.offer.hunt.oh_course.model.entity.CourseMember;
import ru.offer.hunt.oh_course.model.id.CourseMemberId;

import java.util.List;
import java.util.UUID;

public interface CourseMemberRepository extends JpaRepository<CourseMember, CourseMemberId> {

    List<CourseMember> findByIdCourseId(UUID courseId);

    List<CourseMember> findByIdUserId(UUID userId);
}
