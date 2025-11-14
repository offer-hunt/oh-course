package ru.offer.hunt.oh_course.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.offer.hunt.oh_course.model.entity.Rating;
import ru.offer.hunt.oh_course.model.id.RatingId;

import java.util.List;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, RatingId> {

    List<Rating> findByIdCourseId(UUID courseId);

    List<Rating> findByIdUserId(UUID userId);
}
