package ru.offer.hunt.oh_course.service;

import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.*;
import ru.offer.hunt.oh_course.model.entity.*;
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

    public CourseOutlineLiteDto getCourseStructureLite(UUID courseId){
        try{
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));

            ensurePublished(course);
            List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);

            List<LessonOutlineLiteDto> lessonDtos = new ArrayList<>();

            for (Lesson lesson : lessons) {

                List<LessonPage> pages =
                        lessonPageRepository.findByLessonIdOrderBySortOrderAsc(lesson.getId());

                List<LessonPageOutlineLiteDto> pageDtos = new ArrayList<>();

                for (LessonPage page : pages) {

                    List<QuestionOutlineLiteDto> questions =
                            questionRepository.findByPageIdOrderBySortOrderAsc(page.getId())
                                    .stream()
                                    .map(q -> new QuestionOutlineLiteDto(
                                            q.getId(),
                                            q.getType(),
                                            q.getSortOrder()
                                    ))
                                    .toList();

                    pageDtos.add(new LessonPageOutlineLiteDto(
                            page.getId(),
                            page.getTitle(),
                            page.getPageType(),
                            page.getSortOrder(),
                            questions
                    ));
                }

                lessonDtos.add(new LessonOutlineLiteDto(
                        lesson.getId(),
                        lesson.getTitle(),
                        lesson.getOrderIndex(),
                        lesson.getDurationMin(),
                        lesson.isDemo(),
                        false,
                        pageDtos
                ));
            }

            return new CourseOutlineLiteDto(course.getId(), course.getSlug(), lessonDtos);


        }catch (ResponseStatusException e) {
            throw e;
        }catch (Exception ex) {
            log.error("Course get structure failed - server error, courseId={}", courseId, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось выдать страницу. Попробуйте позже."
            );
        }

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
