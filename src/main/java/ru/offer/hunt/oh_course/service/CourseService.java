package ru.offer.hunt.oh_course.service;

import static ru.offer.hunt.oh_course.model.specification.CourseSpecification.withAuthorId;
import static ru.offer.hunt.oh_course.model.specification.CourseSpecification.withDurations;
import static ru.offer.hunt.oh_course.model.specification.CourseSpecification.withLanguages;
import static ru.offer.hunt.oh_course.model.specification.CourseSpecification.withLevels;
import static ru.offer.hunt.oh_course.model.specification.CourseSpecification.withQuery;
import static ru.offer.hunt.oh_course.model.specification.CourseSpecification.withStatus;
import static ru.offer.hunt.oh_course.model.specification.CourseSpecification.withTechnologies;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.CourseDto;
import ru.offer.hunt.oh_course.model.dto.CourseUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.entity.CourseMember;
import ru.offer.hunt.oh_course.model.entity.CourseStats;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.enums.AccessType;
import ru.offer.hunt.oh_course.model.enums.CourseMemberRole;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;
import ru.offer.hunt.oh_course.model.id.CourseMemberId;
import ru.offer.hunt.oh_course.model.mapper.CourseMapper;
import ru.offer.hunt.oh_course.model.mapper.LessonMapper;
import ru.offer.hunt.oh_course.model.repository.CourseMemberRepository;
import ru.offer.hunt.oh_course.model.repository.CourseRepository;
import ru.offer.hunt.oh_course.model.repository.CourseStatsRepository;
import ru.offer.hunt.oh_course.model.repository.LessonPageRepository;
import ru.offer.hunt.oh_course.model.repository.LessonRepository;
import ru.offer.hunt.oh_course.model.search.CourseFilter;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private static final int TITLE_MIN_LEN = 10;
    private static final int TITLE_MAX_LEN = 100;
    private static final int DESCRIPTION_MAX_LEN = 1000;
    private static final int TAG_MAX_LEN = 15;
    private static final int TAGS_MAX_COUNT = 10;

    private final CourseRepository courseRepository;
    private final CourseMemberRepository courseMemberRepository;
    private final CourseStatsRepository courseStatsRepository;
    private final CourseMapper courseMapper;
    private final LessonRepository lessonRepository;
    private final LessonPageRepository lessonPageRepository;
    private final TagService tagService;
    private final LessonMapper lessonMapper;

    public List<CourseDto> getPublishedCourses(CourseFilter courseFilter) {
        CourseFilter f = courseFilter == null ? new CourseFilter() : courseFilter;

        Specification<Course> spec = Specification.where(withStatus(CourseStatus.PUBLISHED))
                .and(withAuthorId(f.getAuthorId()))
                .and(withLanguages(f.getLanguage()))
                .and(withTechnologies(f.getTechnologies()))
                .and(withLevels(f.getLevel()))
                .and(withDurations(f.getDuration()))
                .and(withQuery(f.getQuery()));

        return courseRepository.findAll(spec).stream()
                .map(c -> courseMapper.toDto(c, lessonRepository, lessonMapper, courseStatsRepository))
                .toList();
    }

    public CourseDto getPublishedCourseBySlug(String slug, String inviteCode) {
        Course course = courseRepository.findBySlugAndStatus(slug, CourseStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));

        if (course.getAccessType() == AccessType.PRIVATE_LINK) {
            String expected = course.getInviteCode();
            if (expected == null || inviteCode == null || !expected.equals(inviteCode)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ к этому курсу ограничен");
            }
        }

        return courseMapper.toDto(course, lessonRepository, lessonMapper, courseStatsRepository);
    }

    @Transactional
    public CourseDto createCourse(UUID authorId, CourseUpsertRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        try {
            validateCourseData(request);

            Course course = courseMapper.toEntity(request, tagService);

            course.setId(UUID.randomUUID());
            course.setAuthorId(authorId);
            course.setStatus(CourseStatus.DRAFT);
            course.setVersion(1);
            course.setRequiresEntitlement(false);
            course.setCreatedAt(now);
            course.setUpdatedAt(now);

            courseRepository.save(course);

            // OWNER membership (Course-RBAC)
            createOwnerMembership(course.getId(), authorId, now);

            // stats row (чтобы membersCount не был "null" и всегда был источник истины)
            CourseStats stats = CourseStats.builder()
                    .courseId(course.getId())
                    .enrollments(0)
                    .avgCompletion(java.math.BigDecimal.ZERO)
                    .avgRating(java.math.BigDecimal.ZERO)
                    .updatedAt(now)
                    .build();
            courseStatsRepository.save(stats);

            return courseMapper.toDto(course, lessonRepository, lessonMapper, courseStatsRepository);

        } catch (ResponseStatusException ex) {
            throw ex;

        } catch (DataIntegrityViolationException ex) {
            if (isSlugUniqueViolation(ex)) {
                log.warn("Course creation failed - slug already exists: {}", request.getSlug());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Курс с таким адресом уже существует"
                );
            }
            log.error("Course creation failed - data integrity error", ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось создать курс. Попробуйте позже."
            );

        } catch (Exception ex) {
            log.error("Course creation failed - server error", ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось создать курс. Попробуйте позже."
            );
        }
    }

    @Transactional
    public CourseDto publishCourse(UUID courseId, UUID userId) {
        try {
            Course course =
                    courseRepository
                            .findById(courseId)
                            .orElseThrow(() ->
                                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));

            ensureCourseAdmin(course.getId(), userId);

            if (course.getStatus() == CourseStatus.PUBLISHED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Курс уже опубликован");
            }
            if (course.getStatus() == CourseStatus.ARCHIVED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Заархивированный курс нельзя опубликовать");
            }

            List<String> issues = validateCourseReadyForPublication(course);
            if (!issues.isEmpty()) {
                log.warn("Course publication failed - requirements not met: courseId={}, issues={}",
                        courseId, issues);
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Курс не готов к публикации: " + String.join("; ", issues)
                );
            }

            OffsetDateTime now = OffsetDateTime.now();
            course.setStatus(CourseStatus.PUBLISHED);
            course.setPublishedAt(now);
            course.setUpdatedAt(now);

            courseRepository.save(course);

            log.info("Course published: courseId={}, userId={}", courseId, userId);

            return courseMapper.toDto(course, lessonRepository, lessonMapper, courseStatsRepository);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Course publication failed - server error, courseId={}, userId={}",
                    courseId, userId, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось опубликовать курс. Попробуйте позже.",
                    e);
        }
    }

    private void validateCourseData(CourseUpsertRequest request) {
        validateTitle(request.getTitle());
        validateDescription(request.getDescription());
        validateCover(request.getCoverUrl());
        validateTags(request.getTags());
        validateAccessType(request.getAccessType());
    }

    private void validateTitle(String title) {
        String value = title == null ? "" : title.trim();
        int len = value.length();

        if (len < TITLE_MIN_LEN || len > TITLE_MAX_LEN) {
            log.warn("Course creation failed - invalid title (len={})", len);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Название должно быть от 10 до 100 символов"
            );
        }
    }

    private void validateDescription(String description) {
        String value = description == null ? "" : description.trim();
        int len = value.length();

        if (len == 0 || len > DESCRIPTION_MAX_LEN) {
            log.warn("Course creation failed - invalid description (len={})", len);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Описание должно быть от 1 до 1000 символов"
            );
        }
    }

    private void validateCover(String coverUrl) {
        if (coverUrl == null || coverUrl.isBlank()) return;

        String lower = coverUrl.toLowerCase(Locale.ROOT);
        boolean ok = lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");

        if (!ok) {
            log.warn("Course creation failed - invalid cover: {}", coverUrl);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Неверный формат или размер файла. Максимум 2 МБ, JPG или PNG"
            );
        }
    }

    private void validateTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return;

        if (tags.size() > TAGS_MAX_COUNT) {
            log.warn("Course creation failed - invalid tags count (size={})", tags.size());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не более 10 тегов");
        }

        for (String rawTag : tags) {
            String tag = rawTag == null ? "" : rawTag.trim();
            if (tag.length() > TAG_MAX_LEN) {
                log.warn("Course creation failed - invalid tag: '{}'", tag);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Тег не длиннее 15 символов");
            }
        }
    }

    private void validateAccessType(AccessType accessType) {
        if (accessType == null) {
            log.warn("Course creation failed - accessType is null");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Тип доступа обязателен");
        }
    }

    private void createOwnerMembership(UUID courseId, UUID authorId, OffsetDateTime now) {
        CourseMemberId id = new CourseMemberId(courseId, authorId);
        CourseMember owner = CourseMember.builder()
                .id(id)
                .role(CourseMemberRole.OWNER)
                .addedAt(now)
                .addedBy(authorId)
                .build();

        courseMemberRepository.save(owner);
    }

    private boolean isSlugUniqueViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        if (message == null) return false;

        return message.contains("course_courses_slug_key")
                || message.contains("course_courses_slug_idx")
                || message.toLowerCase(Locale.ROOT).contains("slug");
    }

    private List<String> validateCourseReadyForPublication(Course course) {
        List<String> issues = new ArrayList<>();

        if (course.getTitle() == null || course.getTitle().trim().isEmpty()) issues.add("Добавьте название курса");
        if (course.getDescription() == null || course.getDescription().trim().isEmpty()) issues.add("Добавьте описание курса");
        if (course.getCoverUrl() == null || course.getCoverUrl().trim().isEmpty()) issues.add("Добавьте обложку");

        List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
        if (lessons.isEmpty()) {
            issues.add("Добавьте хотя бы один урок");
            return issues;
        }

        boolean hasLessonWithContent = false;

        for (Lesson lesson : lessons) {
            if (lesson.getTitle() == null || lesson.getTitle().trim().isEmpty()) {
                issues.add("Заполните название урока (id=" + lesson.getId() + ")");
            }
            boolean hasPages = lessonPageRepository.existsByLessonId(lesson.getId());
            if (hasPages) hasLessonWithContent = true;
        }

        if (!hasLessonWithContent) issues.add("Добавьте хотя бы один урок с содержимым");

        return issues;
    }

    private void ensureCourseAdmin(UUID courseId, UUID userId) {
        boolean allowed =
                courseMemberRepository.existsByIdCourseIdAndIdUserIdAndRoleIn(
                        courseId,
                        userId,
                        List.of(CourseMemberRole.OWNER, CourseMemberRole.ADMIN));

        if (!allowed) {
            log.warn("Course publish forbidden: courseId={}, userId={}", courseId, userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав для публикации курса");
        }
    }
}
