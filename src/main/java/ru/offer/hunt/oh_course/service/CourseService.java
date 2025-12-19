package ru.offer.hunt.oh_course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.offer.hunt.oh_course.exception.StatsServiceConnectionException;
import ru.offer.hunt.oh_course.model.dto.CourseDto;
import ru.offer.hunt.oh_course.model.dto.CoursePreviewDto;
import ru.offer.hunt.oh_course.model.dto.LessonDto;
import ru.offer.hunt.oh_course.model.dto.LessonPreviewDto;
import ru.offer.hunt.oh_course.model.entity.LessonPage;
import ru.offer.hunt.oh_course.model.entity.MethodicalPageContent;
import ru.offer.hunt.oh_course.model.entity.Question;
import ru.offer.hunt.oh_course.model.entity.QuestionOption;
import ru.offer.hunt.oh_course.model.entity.QuestionTestCase;
import ru.offer.hunt.oh_course.model.entity.TagRef;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;
import static ru.offer.hunt.oh_course.model.specification.CourseSpecification.withAuthorId;
import static ru.offer.hunt.oh_course.model.specification.CourseSpecification.withDurations;
import static ru.offer.hunt.oh_course.model.specification.CourseSpecification.withLanguages;
import static ru.offer.hunt.oh_course.model.specification.CourseSpecification.withLevels;
import static ru.offer.hunt.oh_course.model.specification.CourseSpecification.withQuery;
import static ru.offer.hunt.oh_course.model.specification.CourseSpecification.withStatus;
import static ru.offer.hunt.oh_course.model.specification.CourseSpecification.withTechnologies;

import ru.offer.hunt.oh_course.model.enums.QuestionType;
import ru.offer.hunt.oh_course.model.id.CourseTagId;
import ru.offer.hunt.oh_course.model.mapper.*;
import ru.offer.hunt.oh_course.model.enums.PageType;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.CourseStatsDto;
import ru.offer.hunt.oh_course.model.dto.CourseUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.entity.CourseMember;
import ru.offer.hunt.oh_course.model.entity.CourseStats;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.enums.AccessType;
import ru.offer.hunt.oh_course.model.enums.CourseMemberRole;
import ru.offer.hunt.oh_course.model.id.CourseMemberId;
import ru.offer.hunt.oh_course.model.mapper.CloningMapper;
import ru.offer.hunt.oh_course.model.mapper.CourseMapper;
import ru.offer.hunt.oh_course.model.mapper.CourseStatsMapper;
import ru.offer.hunt.oh_course.model.mapper.LessonMapper;
import ru.offer.hunt.oh_course.model.repository.CourseMemberRepository;
import ru.offer.hunt.oh_course.model.repository.CourseRepository;
import ru.offer.hunt.oh_course.model.repository.CourseStatsRepository;
import ru.offer.hunt.oh_course.model.repository.LessonPageRepository;
import ru.offer.hunt.oh_course.model.repository.LessonRepository;
import ru.offer.hunt.oh_course.model.repository.MethodicalPageContentRepository;
import ru.offer.hunt.oh_course.model.repository.QuestionOptionRepository;
import ru.offer.hunt.oh_course.model.repository.QuestionRepository;
import ru.offer.hunt.oh_course.model.repository.QuestionTestCaseRepository;
import ru.offer.hunt.oh_course.model.repository.TagRefRepository;
import ru.offer.hunt.oh_course.model.search.CourseFilter;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonPageRepository lessonPageRepository;
    private final LessonMapper lessonMapper;
    private final LessonPageMapper lessonPageMapper;

    private final MethodicalPageContentRepository contentRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionTestCaseRepository questionTestCaseRepository;
    private final TagRefRepository tagRefRepository;

    private final CourseMapper courseMapper;
    private final CloningMapper cloningMapper;

    private static final int TITLE_MIN_LEN = 10;
    private static final int TITLE_MAX_LEN = 100;
    private static final int DESCRIPTION_MAX_LEN = 1000;
    private static final int TAG_MAX_LEN = 15;
    private static final int TAGS_MAX_COUNT = 10;

    private final CourseMemberRepository courseMemberRepository;
    private final CourseStatsRepository courseStatsRepository;
    private final TagService tagService;
    private final CourseStatsMapper courseStatsMapper;

    @Transactional(readOnly = true)
    public List<CourseDto> getPublishedCourses(CourseFilter courseFilter) {
        CourseFilter f = courseFilter == null ? new CourseFilter() : courseFilter;

        Specification<Course> spec = Specification.where(withStatus(CourseStatus.PUBLISHED))
                .and(withAuthorId(f.getAuthorId()))
                .and(withLanguages(f.getLanguage()))
                .and(withTechnologies(f.getTechnologies()))
                .and(withLevels(f.getLevel()))
                .and(withDurations(f.getDuration()))
                .and(withQuery(f.getQuery()));

        return courseRepository.findAll((Sort) spec).stream()
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

            courseRepository.saveAndFlush(course);

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

    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<Course> courses = courseRepository.findAllById(ids);
        if (courses.isEmpty()) {
            return List.of();
        }

        // Только опубликованные (как и каталог/slug)
        Map<UUID, Course> byId = courses.stream()
                .filter(c -> c.getStatus() == CourseStatus.PUBLISHED)
                .collect(Collectors.toMap(Course::getId, c -> c));

        List<CourseDto> result = new ArrayList<>();
        for (UUID id : ids) {
            Course course = byId.get(id);
            if (course != null) {
                result.add(courseMapper.toDto(
                        course,
                        lessonRepository,
                        lessonMapper,
                        courseStatsRepository
                ));
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getMyCourses(UUID userId, List<CourseStatus> statuses) {
        // Все курсы, где пользователь OWNER/ADMIN
        List<CourseMember> memberships = courseMemberRepository.findByIdUserId(userId);
        if (memberships.isEmpty()) {
            return List.of();
        }

        List<UUID> courseIds = memberships.stream()
                .map(m -> m.getId().getCourseId())
                .distinct()
                .toList();

        List<Course> courses = courseRepository.findAllById(courseIds);

        // Фильтрация по статусу, если задано (DRAFT/PUBLISHED/ARCHIVED)
        if (statuses != null && !statuses.isEmpty()) {
            courses = courses.stream()
                    .filter(c -> statuses.contains(c.getStatus()))
                    .toList();
        }

        // Сортировка: самые недавно обновлённые сверху
        courses = courses.stream()
                .sorted((a, b) -> {
                    OffsetDateTime ua = a.getUpdatedAt();
                    OffsetDateTime ub = b.getUpdatedAt();
                    if (ua == null && ub == null) return 0;
                    if (ua == null) return 1;
                    if (ub == null) return -1;
                    return ub.compareTo(ua);
                })
                .toList();

        return courses.stream()
                .map(c -> courseMapper.toDto(c, lessonRepository, lessonMapper, courseStatsRepository))
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseDto getCourseDetails(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Курс не найден"
                ));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден");
        }

        return courseMapper.toDto(course, lessonRepository, lessonMapper, courseStatsRepository);
    }

    @Transactional(readOnly = true)
    public CourseStatsDto getCourseStats(UUID courseId, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Курс не найден"
                ));

        ensureCourseAdmin(course.getId(), userId);

        CourseStats stats = courseStatsRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Статистика курса не найдена"
                ));

        return courseStatsMapper.toDto(stats);
    }

    @Transactional
    public void archiveCourse(UUID courseId, UUID userId) {
        try {
            Course course = getCourseWithAuthCheck(courseId, userId);

            if (course.getStatus() != CourseStatus.PUBLISHED) {
                log.warn("Attempt to archive non-published course. ID: {}", courseId);
                throw new IllegalStateException("Архивировать можно только опубликованный курс");
            }

            course.setStatus(CourseStatus.ARCHIVED);
            course.setArchivedAt(OffsetDateTime.now());
            course.setUpdatedAt(OffsetDateTime.now());
            courseRepository.save(course);

            log.info("Course archived successfully. ID: {}", courseId);

        } catch (Exception e) {
            log.error("Course archivation failed - server error. ID: {}", courseId, e);
            throw e;
        }
    }

    @Transactional
    public void deleteCourse(UUID courseId, UUID userId) {
        try {
            Course course = getCourseWithAuthCheck(courseId, userId);

            if (course.getLessons() != null) {
                for (Lesson lesson : course.getLessons()) {
                    lessonPageRepository.deleteAllByLessonId(lesson.getId());
                }
            }

            course.getTagRefs().clear();
            courseRepository.save(course);

            courseMemberRepository.deleteByIdCourseId(courseId);

            courseStatsRepository.deleteByCourseId(courseId);

            courseRepository.delete(course);

            log.info("Course deleted successfully. ID: {}", courseId);

        } catch (Exception e) {
            log.error("Course deletion failed - server error. ID: {}", courseId, e);
            throw e;
        }
    }

    @Transactional
    public void addTags(UUID courseId, UUID userId, List<UUID> tagIds) {
        try {
            Course course = getCourseWithAuthCheck(courseId, userId);
            if (tagIds == null || tagIds.isEmpty()) {
                return;
            }
            List<TagRef> newTags = tagRefRepository.findAllById(tagIds);

            if (newTags.size() != tagIds.size()) {
                throw new IllegalArgumentException("Некорректные ID тегов");
            }

            long currentCount = course.getTagRefs().size();
            long newUniqueCount = newTags.stream()
                    .filter(newTag -> !course.getTagRefs().contains(newTag))
                    .count();

            if (currentCount + newUniqueCount > 10) {
                throw new IllegalArgumentException("Количество тегов слишком большое");
            }

            boolean changed = false;
            for (TagRef tag : newTags) {
                if (!course.getTagRefs().contains(tag)) {
                    course.getTagRefs().add(tag);
                    changed = true;
                }
            }

            if (changed) {
                courseRepository.save(course);
                log.info("Tags added. CourseID: {}", courseId);
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Tags add failed - server error. CourseID: {}", courseId, e);
            throw new RuntimeException("Не удалось добавить теги. Попробуйте позже.", e);
        }
    }

    @Transactional
    public void removeTag(UUID courseId, UUID userId, UUID tagId) {
        try {
            Course course = getCourseWithAuthCheck(courseId, userId);

            if (course.getTagRefs().size() <= 1) {
                throw new IllegalArgumentException("Количество тегов слишком маленькое, добавьте хотя бы один тег");
            }

            boolean removed = course.getTagRefs().removeIf(tag -> tag.getId().equals(tagId));

            if (removed) {
                courseRepository.save(course);
                log.info("Tags deleted. CourseID: {}, TagID: {}", courseId, tagId);
            } else {
                throw new IllegalArgumentException("Тег не найден у курса");
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Tags delete failed - server error. CourseID: {}", courseId, e);

            throw new RuntimeException("Не удалось удалить теги. Попробуйте позже.", e);
        }
    }

    @Transactional(readOnly = true)
    public CoursePreviewDto getCoursePreview(UUID courseId, UUID userId) {
        try {
            Course course = getCourseWithAuthCheck(courseId, userId);

            if (course.getStatus() != CourseStatus.DRAFT) {
                throw new IllegalStateException("Предпросмотр доступен только для черновиков");
            }

            CoursePreviewDto preview = new CoursePreviewDto();
            preview.setCourse(courseMapper.toDto(course, lessonRepository, lessonMapper, courseStatsRepository));

            List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
            List<LessonPreviewDto> lessonDtos = new ArrayList<>();

            for (Lesson lesson : lessons) {
                LessonDto baseDto = lessonMapper.toDto(lesson);

                LessonPreviewDto lessonPreviewDto = new LessonPreviewDto();

                lessonPreviewDto.setId(baseDto.getId());
                lessonPreviewDto.setCourseId(baseDto.getCourseId());
                lessonPreviewDto.setTitle(baseDto.getTitle());
                lessonPreviewDto.setDescription(baseDto.getDescription());
                lessonPreviewDto.setOrderIndex(baseDto.getOrderIndex());
                lessonPreviewDto.setDurationMin(baseDto.getDurationMin());
                lessonPreviewDto.setCreatedAt(baseDto.getCreatedAt());
                lessonPreviewDto.setUpdatedAt(baseDto.getUpdatedAt());

                List<LessonPage> pages = lessonPageRepository.findByLessonIdOrderBySortOrderAsc(lesson.getId());
                lessonPreviewDto.setPages(pages.stream()
                        .map(lessonPageMapper::toDto)
                        .toList());

                lessonDtos.add(lessonPreviewDto);
            }

            preview.setLessons(lessonDtos);
            return preview;

        } catch (Exception e) {
            log.error("Preview failed - server error. ID: {}", courseId, e);
            throw e;
        }
    }

    @Transactional
    public CourseDto createDraftFromPublished(
            UUID sourceCourseId,
            UUID userId
    ) {
        Course sourceCourse = courseRepository.findById(sourceCourseId)
                .orElseThrow(() -> new IllegalArgumentException("Курс не найден: " + sourceCourseId));

        if (sourceCourse.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalStateException("Черновик можно создать только из опубликованного курса");
        }

        Course draftCourse = new Course();
        draftCourse.setId(UUID.randomUUID());
        draftCourse.setAuthorId(userId);
        draftCourse.setTitle(sourceCourse.getTitle());
        draftCourse.setSlug(sourceCourse.getSlug() + "-draft-" + System.currentTimeMillis());
        draftCourse.setDescription(sourceCourse.getDescription());
        draftCourse.setCoverUrl(sourceCourse.getCoverUrl());
        draftCourse.setLanguage(sourceCourse.getLanguage());
        draftCourse.setLevel(sourceCourse.getLevel());
        draftCourse.setStatus(CourseStatus.DRAFT);
        draftCourse.setVersion(sourceCourse.getVersion() + 1);
        draftCourse.setCreatedAt(OffsetDateTime.now());
        draftCourse.setUpdatedAt(OffsetDateTime.now());

        courseRepository.save(draftCourse);

        copyCourseStructure(sourceCourse.getId(), draftCourse.getId());

        log.info("Draft version created. SourceID: {}, DraftID: {}, Version: {}",
                sourceCourseId, draftCourse.getId(), draftCourse.getVersion());

        return courseMapper.toDto(draftCourse, lessonRepository, lessonMapper, courseStatsRepository);
    }

    @Transactional
    public CourseDto publishDraftCourse(
            UUID draftCourseId,
            UUID userId
    ) {
        Course draftCourse = courseRepository.findById(draftCourseId)
                .orElseThrow(() -> new IllegalArgumentException("Черновик не найден"));

        if (draftCourse.getStatus() != CourseStatus.DRAFT) {
            throw new IllegalStateException("Можно публиковать только черновики");
        }
        validateCourseReadiness(draftCourse);

        try {
            archivePreviousVersion(draftCourse);

            draftCourse.setStatus(CourseStatus.PUBLISHED);
            draftCourse.setPublishedAt(OffsetDateTime.now());
            draftCourse.setUpdatedAt(OffsetDateTime.now());

            courseRepository.save(draftCourse);

            log.info("Course published successfully. ID: {}", draftCourseId);

            return courseMapper.toDto(draftCourse, lessonRepository, lessonMapper, courseStatsRepository);

        } catch (Exception e) {
            if (isConnectionError(e)) {
                throw new StatsServiceConnectionException("Learning & Progress Service connection error - ошибка пересчета статистики");
            }

            log.error("Course publication failed - server error. ID: {}", draftCourseId, e);
            throw new RuntimeException("Не удалось обновить курс. Попробуйте позже");
        }
    }

    private void archivePreviousVersion(Course draftCourse) {
        List<Course> publishedVersions = courseRepository.findAllByAuthorIdAndTitleAndStatus(
                draftCourse.getAuthorId(),
                draftCourse.getTitle(),
                CourseStatus.PUBLISHED
        );

        for (Course oldVersion : publishedVersions) {
            if (!oldVersion.getId().equals(draftCourse.getId())) {
                oldVersion.setStatus(CourseStatus.ARCHIVED);
                oldVersion.setArchivedAt(OffsetDateTime.now());
                oldVersion.setUpdatedAt(OffsetDateTime.now());
                courseRepository.save(oldVersion);
                log.info("Previous version archived. ID: {}", oldVersion.getId());
            }
        }
    }

    private void copyCourseStructure(UUID sourceCourseId, UUID targetCourseId) {
        List<Lesson> sourceLessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(sourceCourseId);
        Course targetCourse = courseRepository.findById(targetCourseId)
                .orElseThrow(() -> new IllegalArgumentException("Курс не найден: " + targetCourseId));

        for (Lesson sourceLesson : sourceLessons) {
            Lesson newLesson = cloningMapper.copyLesson(sourceLesson);

            newLesson.setId(UUID.randomUUID());
            newLesson.setCourse(targetCourse);
            newLesson.setCreatedAt(OffsetDateTime.now());
            newLesson.setUpdatedAt(OffsetDateTime.now());
            lessonRepository.save(newLesson);

            List<LessonPage> sourcePages = lessonPageRepository.findByLessonIdOrderBySortOrderAsc(sourceLesson.getId());
            for (LessonPage sourcePage : sourcePages) {
                LessonPage newPage = cloningMapper.copyPage(sourcePage);

                newPage.setId(UUID.randomUUID());
                newPage.setLesson(newLesson);
                newPage.setCreatedAt(OffsetDateTime.now());
                newPage.setUpdatedAt(OffsetDateTime.now());
                lessonPageRepository.save(newPage);

                copyPageContent(sourcePage.getId(), newPage.getId(), sourcePage.getPageType());
            }
        }
    }

    private void validateCourseReadiness(Course course) {
        if (course.getTitle() == null || course.getDescription() == null || course.getCoverUrl() == null) {
            log.warn("Course publication failed - requirements not met. ID: {}", course.getId());
            throw new IllegalArgumentException("Курс не готов к публикации: заполните название, описание и обложку.");
        }

        boolean hasLessons = !lessonRepository.findByCourseIdOrderByOrderIndexAsc(course.getId()).isEmpty();
        if (!hasLessons) {
            log.warn("Course publication failed - requirements not met (no lessons). ID: {}", course.getId());
            throw new IllegalArgumentException("Курс должен содержать минимум один урок.");
        }
    }

    private void copyPageContent(UUID sourcePageId, UUID newPageId, PageType pageType) {
        switch (pageType) {
            case THEORY -> copyMethodicalContent(sourcePageId, newPageId);
            case TEST, CODE_TASK -> copyQuestions(sourcePageId, newPageId);
        }
    }

    private void copyMethodicalContent(UUID sourcePageId, UUID newPageId) {
        contentRepository.findById(sourcePageId).ifPresent(source -> {
            MethodicalPageContent newContent = cloningMapper.copyMethodical(source);
            newContent.setPageId(newPageId);
            newContent.setUpdatedAt(OffsetDateTime.now());
            contentRepository.save(newContent);
        });
    }

    private void copyQuestions(UUID sourcePageId, UUID newPageId) {
        List<Question> sourceQuestions = questionRepository.findByPageIdOrderBySortOrderAsc(sourcePageId);

        for (Question sourceQ : sourceQuestions) {
            Question newQ = cloningMapper.copyQuestion(sourceQ);
            newQ.setId(UUID.randomUUID());
            newQ.setPageId(newPageId);
            newQ.setCreatedAt(OffsetDateTime.now());
            newQ.setUpdatedAt(OffsetDateTime.now());
            questionRepository.save(newQ);

            copyQuestionOptions(sourceQ.getId(), newQ.getId());
            if (sourceQ.getType() == QuestionType.CODE) {
                copyQuestionTestCases(sourceQ.getId(), newQ.getId());
            }
        }
    }

    private void copyQuestionOptions(UUID sourceQId, UUID newQId) {
        questionOptionRepository.findByQuestionIdOrderBySortOrderAsc(sourceQId).forEach(source -> {
            QuestionOption newOpt = cloningMapper.copyOption(source);
            newOpt.setId(UUID.randomUUID());
            newOpt.setQuestionId(newQId);
            questionOptionRepository.save(newOpt);
        });
    }

    private void copyQuestionTestCases(UUID sourceQId, UUID newQId) {
        questionTestCaseRepository.findByQuestionId(sourceQId).forEach(source -> {
            QuestionTestCase newCase = cloningMapper.copyTestCase(source);
            newCase.setId(UUID.randomUUID());
            newCase.setQuestionId(newQId);
            questionTestCaseRepository.save(newCase);
        });
    }

    private Course getCourseWithAuthCheck(UUID courseId, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Курс не найден: " + courseId));

        if (!course.getAuthorId().equals(userId)) {
            log.warn("Unauthorized access attempt. User: {}, Course: {}", userId, courseId);
            throw new SecurityException("У вас нет прав на редактирование этого курса");
        }
        return course;
    }

    private boolean isConnectionError(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        return msg.contains("connection") || msg.contains("timeout") || msg.contains("learning");
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
