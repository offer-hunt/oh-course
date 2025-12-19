package ru.offer.hunt.oh_course.model.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.offer.hunt.oh_course.model.entity.CourseMember;
import ru.offer.hunt.oh_course.model.enums.CourseMemberRole;
import ru.offer.hunt.oh_course.model.id.CourseMemberId;

public interface CourseMemberRepository extends JpaRepository<CourseMember, CourseMemberId> {

    boolean existsByIdCourseIdAndIdUserIdAndRoleIn(UUID courseId,
                                                   UUID userId,
                                                   Collection<CourseMemberRole> roles);

    boolean existsByIdCourseIdAndIdUserId(UUID courseId, UUID userId);

    List<CourseMember> findByIdCourseId(UUID courseId);

    List<CourseMember> findByIdUserId(UUID userId);

    int countByIdCourseId(UUID courseId);

    void deleteByIdCourseId(UUID courseId);
}
