package ru.offer.hunt.oh_course.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.offer.hunt.oh_course.model.entity.ContentVersion;
import ru.offer.hunt.oh_course.model.enums.VersionScope;

public interface ContentVersionRepository extends JpaRepository<ContentVersion, UUID> {

    List<ContentVersion> findByCourseIdAndScopeOrderByCreatedAtDesc(UUID courseId, VersionScope scope);

    List<ContentVersion> findByLessonIdAndScopeOrderByCreatedAtDesc(UUID lessonId, VersionScope scope);
}
