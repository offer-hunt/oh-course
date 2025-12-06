package ru.offer.hunt.oh_course.model.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.entity.TagRef;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;

public class CourseSpecification {

    private static final int DURATION_TOLERANCE_MIN = 120;

    public static Specification<Course> withAuthorId(UUID authorId) {
        return (root, query, cb) ->
                authorId == null ? null : cb.equal(root.get("authorId"), authorId);
    }

    public static Specification<Course> withLanguages(List<String> langs) {
        return (root, query, cb) ->
                langs == null || langs.isEmpty() ? null : root.get("language").in(langs);
    }

    public static Specification<Course> withLevels(List<String> levels) {
        return (root, query, cb) ->
                levels == null || levels.isEmpty() ? null : root.get("level").in(levels);
    }

    /**
     * technologies = список тегов (AND между категориями делается снаружи, тут только ограничение).
     * Реализовано через EXISTS subquery, чтобы не плодить join'ы и не ловить дубли строк.
     */
    public static Specification<Course> withTechnologies(List<String> techs) {
        return (root, query, cb) -> {
            if (techs == null || techs.isEmpty()) return null;

            Subquery<Long> sq = query.subquery(Long.class);
            Root<Course> sqRoot = sq.from(Course.class);
            Join<Course, TagRef> sqTags = sqRoot.join("tagRefs", JoinType.INNER);

            sq.select(cb.literal(1L));
            sq.where(
                    cb.equal(sqRoot.get("id"), root.get("id")),
                    sqTags.get("name").in(techs)
            );

            return cb.exists(sq);
        };
    }

    /**
     * duration приходит в ЧАСАХ (как в сценариях/макете).
     * В БД estimated_duration_min в МИНУТАХ.
     */
    public static Specification<Course> withDurations(List<Integer> durationsHours) {
        return (root, query, cb) -> {
            if (durationsHours == null || durationsHours.isEmpty()) return null;

            List<Predicate> predicates = new ArrayList<>();
            for (Integer h : durationsHours) {
                if (h == null || h <= 0) continue;
                int targetMin = h * 60;

                predicates.add(cb.between(
                        root.get("estimatedDurationMin"),
                        Math.max(0, targetMin - DURATION_TOLERANCE_MIN),
                        targetMin + DURATION_TOLERANCE_MIN
                ));
            }

            return predicates.isEmpty() ? null : cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Course> withStatus(CourseStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    /**
     * Поиск по title/description/slug + по тегам.
     * tag-search через EXISTS subquery — без join'ов в основном запросе.
     */
    public static Specification<Course> withQuery(String q) {
        return (root, query, cb) -> {
            if (q == null || q.trim().isEmpty()) return null;

            query.distinct(true);

            String needle = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";

            Predicate byTitle = cb.like(cb.lower(root.get("title")), needle);
            Predicate byDesc = cb.like(cb.lower(root.get("description")), needle);
            Predicate bySlug = cb.like(cb.lower(root.get("slug")), needle);

            Subquery<Long> sq = query.subquery(Long.class);
            Root<Course> sqRoot = sq.from(Course.class);
            Join<Course, TagRef> sqTags = sqRoot.join("tagRefs", JoinType.INNER);

            sq.select(cb.literal(1L));
            sq.where(
                    cb.equal(sqRoot.get("id"), root.get("id")),
                    cb.like(cb.lower(sqTags.get("name")), needle)
            );

            Predicate byTag = cb.exists(sq);

            return cb.or(byTitle, byDesc, bySlug, byTag);
        };
    }
}
