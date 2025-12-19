package ru.offer.hunt.oh_course.service;

import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.CourseOutlineDto;
import ru.offer.hunt.oh_course.model.dto.CtaDto;
import ru.offer.hunt.oh_course.model.dto.LessonOutlineDto;
import ru.offer.hunt.oh_course.model.dto.LessonPageShortDto;
import ru.offer.hunt.oh_course.model.dto.MethodicalContentDto;
import ru.offer.hunt.oh_course.model.dto.PageViewDto;
import ru.offer.hunt.oh_course.model.dto.QuestionOptionViewDto;
import ru.offer.hunt.oh_course.model.dto.QuestionViewDto;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.entity.CourseQuestion;
import ru.offer.hunt.oh_course.model.entity.CourseQuestionOption;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.entity.LessonPage;
import ru.offer.hunt.oh_course.model.entity.MethodicalPageContent;
import ru.offer.hunt.oh_course.model.enums.AccessType;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;
import ru.offer.hunt.oh_course.model.enums.PageType;
import ru.offer.hunt.oh_course.model.enums.QuestionType;
import ru.offer.hunt.oh_course.model.repository.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseContentService {

    private static final CtaDto ENROLL_CTA = CtaDto.builder()
            .type("ENROLL")
            .text("Записаться на курс, чтобы получить полный доступ и выполнять задания")
            .build();

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonPageRepository lessonPageRepository;
    private final MethodicalPageContentRepository methodicalRepo;
    private final CourseQuestionRepository questionRepository;
    private final CourseQuestionOptionRepository optionRepository;

    public CourseOutlineDto getCourseOutline(String slug, String inviteCode) {
        Course course = courseRepository.findBySlugAndStatus(slug, CourseStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));

        ensureInviteIfPrivate(course, inviteCode);

        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());

        List<LessonOutlineDto> out = lessons.stream()
                .map(l -> LessonOutlineDto.builder()
                        .id(l.getId())
                        .title(l.getTitle())
                        .orderIndex(l.getOrderIndex())
                        .durationMin(l.getDurationMin())
                        .isDemo(l.isDemo())
                        .locked(!l.isDemo())
                        .build())
                .toList();

        return CourseOutlineDto.builder()
                .courseId(course.getId())
                .slug(course.getSlug())
                .lessons(out)
                .build();
    }

    public List<LessonPageShortDto> getDemoLessonPages(UUID lessonId, String inviteCode) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Урок не найден"));

        Course course = lesson.getCourse();
        ensurePublished(course);
        ensureInviteIfPrivate(course, inviteCode);
        ensureDemoLesson(lesson);

        return lessonPageRepository.findByLessonIdOrderBySortOrderAsc(lessonId).stream()
                .map(p -> LessonPageShortDto.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .pageType(p.getPageType())
                        .sortOrder(p.getSortOrder())
                        .build())
                .toList();
    }

    public PageViewDto getDemoPageView(UUID pageId, String inviteCode) {
        LessonPage page = lessonPageRepository.findById(pageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена"));

        Lesson lesson = page.getLesson();
        Course course = lesson.getCourse();

        ensurePublished(course);
        ensureInviteIfPrivate(course, inviteCode);
        ensureDemoLesson(lesson);

        PageType type = page.getPageType();

        // THEORY
        if (type == PageType.THEORY) {
            MethodicalPageContent content = methodicalRepo.findById(pageId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Контент страницы не найден"));

            return PageViewDto.builder()
                    .pageId(page.getId())
                    .title(page.getTitle())
                    .pageType(type)
                    .readOnly(true)
                    .methodical(MethodicalContentDto.builder()
                            .markdown(content.getMarkdown())
                            .externalVideoUrl(content.getExternalVideoUrl())
                            .build())
                    .questions(List.of())
                    .cta(ENROLL_CTA)
                    .build();
        }

        // TEST / CODE_TASK (read-only + без секретов!)
        if (type == PageType.TEST || type == PageType.CODE_TASK) {
            List<CourseQuestion> questions = questionRepository.findByPageIdOrderBySortOrderAsc(pageId);

            List<UUID> qIds = questions.stream().map(CourseQuestion::getId).toList();
            Map<UUID, List<CourseQuestionOption>> optionsByQ = new HashMap<>();

            if (!qIds.isEmpty()) {
                List<CourseQuestionOption> options = optionRepository.findAllByQuestionIdsOrdered(qIds);
                for (CourseQuestionOption o : options) {
                    optionsByQ.computeIfAbsent(o.getQuestion().getId(), k -> new ArrayList<>()).add(o);
                }
            }

            List<QuestionViewDto> outQuestions = questions.stream()
                    .map(q -> QuestionViewDto.builder()
                            .id(q.getId())
                            .type(q.getType())
                            .text(q.getText())
                            .options(mapOptionsForView(q, optionsByQ.getOrDefault(q.getId(), List.of())))
                            .build())
                    .toList();

            return PageViewDto.builder()
                    .pageId(page.getId())
                    .title(page.getTitle())
                    .pageType(type)
                    .readOnly(true)
                    .methodical(null)
                    .questions(outQuestions)
                    .cta(ENROLL_CTA)
                    .build();
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неизвестный тип страницы");
    }

    private List<QuestionOptionViewDto> mapOptionsForView(CourseQuestion q, List<CourseQuestionOption> options) {
        // options имеет смысл только для choice-вопросов
        if (q.getType() != QuestionType.SINGLE_CHOICE && q.getType() != QuestionType.MULTIPLE_CHOICE) {
            return List.of();
        }

        return options.stream()
                .map(o -> QuestionOptionViewDto.builder()
                        .id(o.getId())
                        .label(o.getLabel())
                        .sortOrder(o.getSortOrder())
                        .build())
                .toList();
    }

    private void ensurePublished(Course course) {
        if (course == null || course.getStatus() != CourseStatus.PUBLISHED) {
            // чтобы не светить существование черновиков — делаем как "не найдено"
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден");
        }
    }

    private void ensureDemoLesson(Lesson lesson) {
        if (!lesson.isDemo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Этот урок доступен только после записи на курс");
        }
    }

    private void ensureInviteIfPrivate(Course course, String inviteCode) {
        if (course.getAccessType() == AccessType.PRIVATE_LINK) {
            String expected = course.getInviteCode();
            if (expected == null || inviteCode == null || !expected.equals(inviteCode)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ к этому курсу ограничен");
            }
        }
    }
}
