package ru.offer.hunt.oh_course.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.CourseStructureDto;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.entity.LessonPage;
import ru.offer.hunt.oh_course.model.entity.MethodicalPageContent;
import ru.offer.hunt.oh_course.model.entity.Question;
import ru.offer.hunt.oh_course.model.entity.QuestionOption;
import ru.offer.hunt.oh_course.model.entity.QuestionTestCase;
import ru.offer.hunt.oh_course.model.repository.CourseRepository;
import ru.offer.hunt.oh_course.model.repository.LessonPageRepository;
import ru.offer.hunt.oh_course.model.repository.LessonRepository;
import ru.offer.hunt.oh_course.model.repository.MethodicalPageContentRepository;
import ru.offer.hunt.oh_course.model.repository.QuestionOptionRepository;
import ru.offer.hunt.oh_course.model.repository.QuestionRepository;
import ru.offer.hunt.oh_course.model.repository.QuestionTestCaseRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseStructureService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonPageRepository lessonPageRepository;
    private final MethodicalPageContentRepository methodicalPageContentRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionTestCaseRepository questionTestCaseRepository;

    public CourseStructureDto getCourseStructure(UUID courseId) {
        Course course =
                courseRepository
                        .findById(courseId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<UUID> lessonIds = lessons.stream().map(Lesson::getId).toList();

        List<LessonPage> pages =
                lessonIds.isEmpty()
                        ? List.of()
                        : lessonPageRepository.findByLessonIdInOrderBySortOrderAsc(lessonIds);

        Map<UUID, List<LessonPage>> pagesByLesson =
                pages.stream().collect(groupingBy(p -> p.getLesson().getId()));

        List<UUID> pageIds = pages.stream().map(LessonPage::getId).toList();

        Map<UUID, MethodicalPageContent> methodicalByPage =
                pageIds.isEmpty()
                        ? Map.of()
                        : methodicalPageContentRepository.findAllById(pageIds).stream()
                        .collect(toMap(MethodicalPageContent::getPageId, x -> x, (a, b) -> a));

        List<Question> questions =
                pageIds.isEmpty() ? List.of() : questionRepository.findByPageIdInOrderBySortOrderAsc(pageIds);

        Map<UUID, List<Question>> questionsByPage =
                questions.stream().collect(groupingBy(Question::getPageId));

        List<UUID> questionIds = questions.stream().map(Question::getId).toList();

        Map<UUID, List<QuestionOption>> optionsByQuestion =
                questionIds.isEmpty()
                        ? Map.of()
                        : questionOptionRepository.findByQuestionIdInOrderBySortOrderAsc(questionIds).stream()
                        .collect(groupingBy(QuestionOption::getQuestionId));

        Map<UUID, List<QuestionTestCase>> testCasesByQuestion =
                questionIds.isEmpty()
                        ? Map.of()
                        : questionTestCaseRepository.findByQuestionIdIn(questionIds).stream()
                        .collect(groupingBy(QuestionTestCase::getQuestionId));

        List<CourseStructureDto.LessonDto> lessonDtos =
                lessons.stream()
                        .map(
                                l -> {
                                    List<LessonPage> lessonPages =
                                            pagesByLesson.getOrDefault(l.getId(), Collections.emptyList());

                                    List<CourseStructureDto.PageDto> pageDtos =
                                            lessonPages.stream()
                                                    .map(
                                                            p -> {
                                                                MethodicalPageContent mc = methodicalByPage.get(p.getId());

                                                                CourseStructureDto.MethodicalContentDto mcDto =
                                                                        mc == null
                                                                                ? null
                                                                                : new CourseStructureDto.MethodicalContentDto(
                                                                                mc.getMarkdown(), mc.getExternalVideoUrl(), mc.getUpdatedAt());

                                                                List<Question> pageQuestions =
                                                                        questionsByPage.getOrDefault(p.getId(), Collections.emptyList());

                                                                List<CourseStructureDto.QuestionDto> qDtos =
                                                                        pageQuestions.stream()
                                                                                .map(
                                                                                        q -> {
                                                                                            List<CourseStructureDto.QuestionOptionDto> optDtos =
                                                                                                    optionsByQuestion
                                                                                                            .getOrDefault(q.getId(), List.of())
                                                                                                            .stream()
                                                                                                            .map(
                                                                                                                    o ->
                                                                                                                            new CourseStructureDto.QuestionOptionDto(
                                                                                                                                    o.getId(),
                                                                                                                                    o.getLabel(),
                                                                                                                                    o.isCorrect(),
                                                                                                                                    o.getSortOrder()))
                                                                                                            .toList();

                                                                                            List<CourseStructureDto.QuestionTestCaseDto> tcDtos =
                                                                                                    testCasesByQuestion
                                                                                                            .getOrDefault(q.getId(), List.of())
                                                                                                            .stream()
                                                                                                            .map(
                                                                                                                    tc ->
                                                                                                                            new CourseStructureDto.QuestionTestCaseDto(
                                                                                                                                    tc.getId(),
                                                                                                                                    tc.getInputData(),
                                                                                                                                    tc.getExpectedOutput(),
                                                                                                                                    tc.getTimeoutMs(),
                                                                                                                                    tc.getMemoryLimitMb()))
                                                                                                            .toList();

                                                                                            return new CourseStructureDto.QuestionDto(
                                                                                                    q.getId(),
                                                                                                    q.getType().name(),
                                                                                                    q.getText(),
                                                                                                    q.getCorrectAnswer(),
                                                                                                    q.isUseAiCheck(),
                                                                                                    q.getPoints(),
                                                                                                    q.getSortOrder(),
                                                                                                    optDtos,
                                                                                                    tcDtos);
                                                                                        })
                                                                                .toList();

                                                                return new CourseStructureDto.PageDto(
                                                                        p.getId(),
                                                                        p.getTitle(),
                                                                        p.getPageType().name(),
                                                                        p.getSortOrder(),
                                                                        mcDto,
                                                                        qDtos);
                                                            })
                                                    .toList();

                                    return new CourseStructureDto.LessonDto(
                                            l.getId(),
                                            l.getTitle(),
                                            l.getDescription(),
                                            l.getOrderIndex(),
                                            l.getDurationMin(),
                                            l.isDemo(),
                                            pageDtos);
                                })
                        .toList();

        return new CourseStructureDto(
                course.getId(),
                course.getTitle(),
                course.getVersion(),
                course.getStatus().name(),
                course.getUpdatedAt(),
                lessonDtos);
    }
}
