package ru.offer.hunt.oh_course.model.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.entity.TagRef;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;

import java.util.List;
import java.util.UUID;

public class CourseSpecification {

    public static Specification<Course> withAuthorId(UUID authorId) {
        return (root, query, cb) ->
                authorId == null
                        ? null
                        : cb.equal(root.get("authorId"), authorId);
    }

    public static Specification<Course> withLanguages(List<String> langs) {
        return (root, query, cb) ->
                langs == null || langs.isEmpty()
                        ? null
                        : root.get("language").in(langs);
    }

    public static Specification<Course> withLevels(List<String> levels) {
        return (root, query, cb) ->
                levels == null || levels.isEmpty()
                        ? null
                        : root.get("level").in(levels);
    }

    public static Specification<Course> withTechnologies(List<String> techs) {
        return (root, query, cb) -> {
            if (techs == null || techs.isEmpty()) return null;

            Join<Course, TagRef> tags = root.join("tagRefs", JoinType.LEFT);

            return tags.get("name").in(techs);
        };
    }

    public static Specification<Course> withDurations(List<Integer> durations) {
        return (root, query, cb) -> {
            if (durations == null || durations.isEmpty()) return null;

            List<Predicate> predicates = durations.stream()
                    .map(d -> cb.between(
                            root.get("estimatedDurationMin"),
                            d - 120,
                            d + 120
                    ))
                    .toList();

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Course> withStatus(CourseStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Course> withQuery(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isEmpty()) {
                return null;
            }

            Join<Course, TagRef> tags = root.join("tagRefs", JoinType.LEFT);
            return cb.or(cb.like(cb.lower(tags.get("name")), "%" + q.toLowerCase() + "%"),
                   cb.like(cb.lower(root.get("title")), "%" + q.toLowerCase() + "%"),
                   cb.like(cb.lower(root.get("description")), "%" + q.toLowerCase() + "%"),
                   cb.like(cb.lower(root.get("slug")), "%" + q.toLowerCase() + "%"));
        };
    }

}
