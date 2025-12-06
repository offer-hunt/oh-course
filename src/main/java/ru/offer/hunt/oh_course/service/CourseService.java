package ru.offer.hunt.oh_course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.offer.hunt.oh_course.exception.StatsServiceConnectionException;
import ru.offer.hunt.oh_course.model.dto.CourseDto;
import ru.offer.hunt.oh_course.model.dto.CoursePreviewDto;
import ru.offer.hunt.oh_course.model.dto.LessonDto;
import ru.offer.hunt.oh_course.model.dto.LessonPreviewDto;
import ru.offer.hunt.oh_course.model.entity.*;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;
import ru.offer.hunt.oh_course.model.enums.QuestionType;
import ru.offer.hunt.oh_course.model.id.CourseTagId;
import ru.offer.hunt.oh_course.model.mapper.*;
import ru.offer.hunt.oh_course.model.repository.*;
import ru.offer.hunt.oh_course.model.enums.PageType;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonPageRepository lessonPageRepository;
    private final MethodicalPageContentRepository contentRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionTestCaseRepository questionTestCaseRepository;
    private final CourseTagRepository courseTagRepository;
    private final TagRefRepository tagRefRepository;

    private final CourseMapper courseMapper;
    private final CloningMapper cloningMapper;
    private final LessonMapper lessonMapper;
    private final LessonPageMapper lessonPageMapper;

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
            getCourseWithAuthCheck(courseId, userId);

            long currentTagsCount = courseTagRepository.findByIdCourseId(courseId).size();
            if (currentTagsCount + tagIds.size() > 10) {
                log.warn("Tags limit exceeded. CourseID: {}", courseId);
                throw new IllegalArgumentException("Количество тегов слишком большое (максимум 10)");
            }

            List<TagRef> existingTags = tagRefRepository.findAllById(tagIds);
            if (existingTags.size() != tagIds.size()) {
                throw new IllegalArgumentException("Некорректные ID тегов");
            }

            for (UUID tagId : tagIds) {
                CourseTagId id = new CourseTagId(courseId, tagId);
                if (!courseTagRepository.existsById(id)) {
                    CourseTag courseTag = new CourseTag();
                    courseTag.setId(id);
                    courseTagRepository.save(courseTag);
                }
            }

            log.info("Tags added. CourseID: {}, Tags: {}", courseId, tagIds);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Tags add failed - server error. CourseID: {}", courseId, e);
            throw new RuntimeException("Не удалось добавить теги", e);
        }
    }

    @Transactional
    public void removeTag(UUID courseId, UUID userId, UUID tagId) {
        try {
            getCourseWithAuthCheck(courseId, userId);

            List<CourseTag> currentTags = courseTagRepository.findByIdCourseId(courseId);
            if (currentTags.size() <= 1) {
                log.warn("Tags limit too low. CourseID: {}", courseId);
                throw new IllegalArgumentException("Количество тегов слишком маленькое, добавьте хотя бы один тег");
            }

            CourseTagId id = new CourseTagId(courseId, tagId);
            if (courseTagRepository.existsById(id)) {
                courseTagRepository.deleteById(id);
                log.info("Tags deleted. CourseID: {}, TagID: {}", courseId, tagId);
            } else {
                throw new IllegalArgumentException("Тег не найден у данного курса");
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Tags delete failed - server error. CourseID: {}", courseId, e);
            throw new RuntimeException("Не удалось удалить теги", e);
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
            preview.setCourse(courseMapper.toDto(course));

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

        return courseMapper.toDto(draftCourse);
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

            return courseMapper.toDto(draftCourse);

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

        for (Lesson sourceLesson : sourceLessons) {
            Lesson newLesson = cloningMapper.copyLesson(sourceLesson);

            newLesson.setId(UUID.randomUUID());
            newLesson.setCourseId(targetCourseId);
            newLesson.setCreatedAt(OffsetDateTime.now());
            newLesson.setUpdatedAt(OffsetDateTime.now());
            lessonRepository.save(newLesson);

            List<LessonPage> sourcePages = lessonPageRepository.findByLessonIdOrderBySortOrderAsc(sourceLesson.getId());
            for (LessonPage sourcePage : sourcePages) {
                LessonPage newPage = cloningMapper.copyPage(sourcePage);

                newPage.setId(UUID.randomUUID());
                newPage.setLessonId(newLesson.getId());
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
}
