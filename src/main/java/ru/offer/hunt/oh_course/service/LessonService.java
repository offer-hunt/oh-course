package ru.offer.hunt.oh_course.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.LessonCreateRequest;
import ru.offer.hunt.oh_course.model.dto.LessonDto;
import ru.offer.hunt.oh_course.model.dto.LessonPageDto;
import ru.offer.hunt.oh_course.model.dto.LessonPageUpsertRequest;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentDto;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentUpsertRequest;
import ru.offer.hunt.oh_course.model.dto.QuestionDto;
import ru.offer.hunt.oh_course.model.dto.QuestionOptionDto;
import ru.offer.hunt.oh_course.model.dto.QuestionOptionUpsertRequest;
import ru.offer.hunt.oh_course.model.dto.QuestionUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.entity.LessonPage;
import ru.offer.hunt.oh_course.model.entity.MethodicalPageContent;
import ru.offer.hunt.oh_course.model.entity.Question;
import ru.offer.hunt.oh_course.model.entity.QuestionOption;
import ru.offer.hunt.oh_course.model.enums.CourseMemberRole;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;
import ru.offer.hunt.oh_course.model.enums.PageType;
import ru.offer.hunt.oh_course.model.enums.QuestionType;
import ru.offer.hunt.oh_course.model.mapper.LessonMapper;
import ru.offer.hunt.oh_course.model.mapper.LessonPageMapper;
import ru.offer.hunt.oh_course.model.mapper.MethodicalPageContentMapper;
import ru.offer.hunt.oh_course.model.mapper.QuestionMapper;
import ru.offer.hunt.oh_course.model.mapper.QuestionOptionMapper;
import ru.offer.hunt.oh_course.model.repository.CourseMemberRepository;
import ru.offer.hunt.oh_course.model.repository.CourseRepository;
import ru.offer.hunt.oh_course.model.repository.LessonPageRepository;
import ru.offer.hunt.oh_course.model.repository.LessonRepository;
import ru.offer.hunt.oh_course.model.repository.MethodicalPageContentRepository;
import ru.offer.hunt.oh_course.model.repository.QuestionOptionRepository;
import ru.offer.hunt.oh_course.model.repository.QuestionRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonService {

    private final CourseRepository courseRepository;
    private final CourseMemberRepository courseMemberRepository;
    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;

    private final LessonPageRepository lessonPageRepository;
    private final LessonPageMapper lessonPageMapper;

    private final MethodicalPageContentRepository methodicalPageContentRepository;
    private final MethodicalPageContentMapper methodicalPageContentMapper;

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionOptionMapper questionOptionMapper;

    @Transactional
    public LessonDto createLesson(UUID courseId, UUID userId, LessonCreateRequest req) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));

        ensureCourseAdmin(courseId, userId);

        if (course.getStatus() == CourseStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя добавлять уроки в архивированный курс");
        }

        OffsetDateTime now = OffsetDateTime.now();
        boolean demo = Boolean.TRUE.equals(req.getDemo());

        Lesson lesson = Lesson.builder()
                .id(UUID.randomUUID())
                .course(course)
                .title(req.getTitle().trim())
                .description(req.getDescription())
                .orderIndex(req.getOrderIndex())
                .durationMin(req.getDurationMin())
                .demo(demo)
                .createdAt(now)
                .updatedAt(now)
                .build();

        lessonRepository.save(lesson);

        log.info("Lesson created: lessonId={}, courseId={}, userId={}, demo={}",
                lesson.getId(), courseId, userId, demo);

        return lessonMapper.toDto(lesson);
    }

    @Transactional(readOnly = true)
    public List<LessonDto> listLessons(UUID courseId, UUID userId) {
        // чтобы не светить чужие черновики
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));

        ensureCourseAdmin(course.getId(), userId);

        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(lessonMapper::toDto)
                .toList();
    }

    private void ensureCourseAdmin(UUID courseId, UUID userId) {
        boolean allowed =
                courseMemberRepository.existsByIdCourseIdAndIdUserIdAndRoleIn(
                        courseId,
                        userId,
                        List.of(CourseMemberRole.OWNER, CourseMemberRole.ADMIN));

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав для управления курсом");
        }
    }

    @Transactional
    public LessonPageDto createLessonPage(UUID lessonId, UUID userId, LessonPageUpsertRequest req) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Урок не найден"));

        UUID courseId = lesson.getCourse().getId();
        ensureCourseAdmin(courseId, userId);

        if (lesson.getCourse().getStatus() == CourseStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя редактировать архивированный курс");
        }

        OffsetDateTime now = OffsetDateTime.now();

        LessonPage page = lessonPageMapper.toEntity(req);
        page.setId(UUID.randomUUID());
        page.setLesson(lesson);
        page.setCreatedAt(now);
        page.setUpdatedAt(now);

        lessonPageRepository.save(page);

        log.info("Lesson page created: pageId={}, lessonId={}, courseId={}, userId={}",
                page.getId(), lessonId, courseId, userId);

        return lessonPageMapper.toDto(page);
    }

    public record UpsertResult<T>(boolean created, T dto) {}

    @Transactional
    public UpsertResult<MethodicalPageContentDto> upsertMethodical(
            UUID pageId,
            UUID userId,
            MethodicalPageContentUpsertRequest req
    ) {
        LessonPage page = lessonPageRepository.findById(pageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена"));

        UUID courseId = page.getLesson().getCourse().getId();
        ensureCourseAdmin(courseId, userId);

        if (page.getLesson().getCourse().getStatus() == CourseStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя редактировать архивированный курс");
        }

        if (page.getPageType() != PageType.THEORY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Методичка доступна только для THEORY-страниц");
        }

        OffsetDateTime now = OffsetDateTime.now();

        MethodicalPageContent content = methodicalPageContentRepository.findById(pageId).orElse(null);
        boolean created = (content == null);

        if (created) {
            content = methodicalPageContentMapper.toEntity(pageId, req);
        } else {
            methodicalPageContentMapper.update(content, req);
        }
        content.setUpdatedAt(now);

        MethodicalPageContent saved = methodicalPageContentRepository.save(content);

        log.info("Methodical upsert: pageId={}, courseId={}, userId={}, created={}",
                pageId, courseId, userId, created);

        return new UpsertResult<>(created, methodicalPageContentMapper.toDto(saved));
    }

    @Transactional
    public QuestionDto createQuestion(UUID pageId, UUID userId, QuestionUpsertRequest req) {
        LessonPage page = lessonPageRepository.findById(pageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена"));

        UUID courseId = page.getLesson().getCourse().getId();
        ensureCourseAdmin(courseId, userId);

        if (page.getLesson().getCourse().getStatus() == CourseStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя редактировать архивированный курс");
        }

        if (page.getPageType() != PageType.TEST) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вопросы можно добавлять только на TEST-страницы");
        }

        OffsetDateTime now = OffsetDateTime.now();

        Question q = questionMapper.toEntity(pageId, req);
        q.setId(UUID.randomUUID());
        q.setCreatedAt(now);
        q.setUpdatedAt(now);

        Question saved = questionRepository.save(q);

        log.info("Question created: questionId={}, pageId={}, courseId={}, userId={}",
                saved.getId(), pageId, courseId, userId);

        return questionMapper.toDto(saved);
    }

    @Transactional
    public QuestionOptionDto createQuestionOption(UUID questionId, UUID userId, QuestionOptionUpsertRequest req) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вопрос не найден"));

        LessonPage page = lessonPageRepository.findById(question.getPageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Страница вопроса не найдена"));

        UUID courseId = page.getLesson().getCourse().getId();
        ensureCourseAdmin(courseId, userId);

        if (page.getLesson().getCourse().getStatus() == CourseStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя редактировать архивированный курс");
        }

        if (question.getType() != QuestionType.SINGLE_CHOICE && question.getType() != QuestionType.MULTIPLE_CHOICE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Опции допустимы только для choice-вопросов");
        }

        QuestionOption opt = questionOptionMapper.toEntity(questionId, req);
        opt.setId(UUID.randomUUID());

        QuestionOption saved = questionOptionRepository.save(opt);

        log.info("Option created: optionId={}, questionId={}, courseId={}, userId={}",
                saved.getId(), questionId, courseId, userId);

        return questionOptionMapper.toDto(saved);
    }

}
