package ru.offer.hunt.oh_course.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.CodeTaskDto;
import ru.offer.hunt.oh_course.model.dto.CodeTaskTestCaseDto;
import ru.offer.hunt.oh_course.model.dto.CodeTaskTestCaseRequest;
import ru.offer.hunt.oh_course.model.dto.CodeTaskUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.entity.LessonPage;
import ru.offer.hunt.oh_course.model.entity.Question;
import ru.offer.hunt.oh_course.model.entity.QuestionTestCase;
import ru.offer.hunt.oh_course.model.enums.CodeLanguage;
import ru.offer.hunt.oh_course.model.enums.CourseMemberRole;
import ru.offer.hunt.oh_course.model.enums.PageType;
import ru.offer.hunt.oh_course.model.enums.QuestionType;
import ru.offer.hunt.oh_course.model.repository.CourseMemberRepository;
import ru.offer.hunt.oh_course.model.repository.LessonPageRepository;
import ru.offer.hunt.oh_course.model.repository.LessonRepository;
import ru.offer.hunt.oh_course.model.repository.QuestionRepository;
import ru.offer.hunt.oh_course.model.repository.QuestionTestCaseRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeTaskService {

    private static final int MAX_DESCRIPTION_LENGTH = 5000;
    private static final int MAX_TEST_TEXT_CHARS = 5000;

    private final LessonPageRepository lessonPageRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final QuestionTestCaseRepository questionTestCaseRepository;
    private final CourseMemberRepository courseMemberRepository;

    @Transactional
    public CodeTaskDto saveCodeTask(UUID pageId, UUID userId, CodeTaskUpsertRequest req) {
        try {
            LessonPage page = lessonPageRepository.findById(pageId)
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена"));

            if (page.getPageType() != PageType.CODE_TASK) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Страница не предназначена для кодовых заданий"
                );
            }

            UUID lessonId = page.getLessonId();
            if (lessonId == null) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Не удалось определить урок для страницы кодового задания"
                );
            }

            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "Не удалось найти урок для страницы кодового задания"
                            ));

            UUID courseId = lesson.getCourseId();
            if (courseId == null) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Не удалось определить курс для страницы кодового задания"
                );
            }

            ensureCanEditCodeTasks(courseId, userId);

            // Валидация
            validateDescription(req.getDescription());
            validateLanguage(req.getLanguage());
            validateTests(pageId, req.getTestCases());

            OffsetDateTime now = OffsetDateTime.now();

            List<Question> existingCodeQuestions = questionRepository.findByPageId(pageId).stream()
                    .filter(q -> q.getType() == QuestionType.CODE)
                    .toList();

            Question question;
            if (existingCodeQuestions.isEmpty()) {
                question = Question.builder()
                        .id(UUID.randomUUID())
                        .pageId(pageId)
                        .type(QuestionType.CODE)
                        .text(req.getDescription())
                        .useAiCheck(false)
                        .sortOrder(1)
                        .createdAt(now)
                        .updatedAt(null)
                        .build();
            } else {
                question = existingCodeQuestions.getFirst();
                question.setText(req.getDescription());
                question.setUpdatedAt(now);

                questionTestCaseRepository.deleteByQuestionId(question.getId());
            }

            CodeLanguage language = req.getLanguage();
            try {
                question.getClass().getMethod("setLanguage", CodeLanguage.class)
                        .invoke(question, language);
            } catch (ReflectiveOperationException ignored) {
            }

            Question savedQuestion = questionRepository.save(question);

            List<QuestionTestCase> testEntities = new ArrayList<>();
            for (CodeTaskTestCaseRequest tcReq : req.getTestCases()) {
                QuestionTestCase testCase = QuestionTestCase.builder()
                        .id(UUID.randomUUID())
                        .questionId(savedQuestion.getId())
                        .inputData(tcReq.getInputData())
                        .expectedOutput(tcReq.getExpectedOutput())
                        .build();
                testEntities.add(testCase);
            }
            List<QuestionTestCase> savedTests = questionTestCaseRepository.saveAll(testEntities);

            log.info(
                    "Code page added: courseId={}, lessonId={}, pageId={}, questionId={}, testsCount={}",
                    courseId,
                    lessonId,
                    pageId,
                    savedQuestion.getId(),
                    savedTests.size()
            );

            CodeTaskDto dto = new CodeTaskDto();
            dto.setQuestionId(savedQuestion.getId());
            dto.setPageId(pageId);
            dto.setDescription(req.getDescription());
            dto.setLanguage(req.getLanguage());

            List<CodeTaskTestCaseDto> testDtos = new ArrayList<>();
            for (QuestionTestCase t : savedTests) {
                CodeTaskTestCaseDto td = new CodeTaskTestCaseDto();
                td.setId(t.getId());
                td.setInputData(t.getInputData());
                td.setExpectedOutput(t.getExpectedOutput());
                testDtos.add(td);
            }
            dto.setTestCases(testDtos);

            return dto;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    "Code page add failed - server error, pageId={}, userId={}",
                    pageId,
                    userId,
                    e
            );
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось добавить кодовое задание. Попробуйте позже.",
                    e
            );
        }
    }

    private void validateDescription(String description) {
        String value = description == null ? "" : description.trim();
        if (value.isEmpty()) {
            log.warn("Code page add failed - invalid description (empty)");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Поле с текстом задания не может быть пустым"
            );
        }
        if (value.length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Code page add failed - invalid description (too long, len={})",
                    value.length());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Слишком длинный текст"
            );
        }
    }

    private void validateLanguage(CodeLanguage language) {
        if (language == null) {
            log.warn("Code page add failed - invalid language (null)");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Не выбран язык программирования"
            );
        }
    }

    private void validateTests(UUID pageId, List<CodeTaskTestCaseRequest> tests) {
        if (tests == null || tests.isEmpty()) {
            log.warn("Code page add failed - no tests, pageId={}", pageId);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Не добавлены тесты"
            );
        }

        List<String> tooBigNames = new ArrayList<>();

        for (int i = 0; i < tests.size(); i++) {
            CodeTaskTestCaseRequest tc = tests.get(i);
            String input = tc.getInputData() == null ? "" : tc.getInputData();
            String output = tc.getExpectedOutput() == null ? "" : tc.getExpectedOutput();

            if (input.isBlank() || output.isBlank()) {
                log.warn(
                        "Code page add failed - invalid tests format, pageId={}, index={}",
                        pageId,
                        i
                );
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Некорректный формат тестов"
                );
            }

            if (input.length() > MAX_TEST_TEXT_CHARS) {
                tooBigNames.add("test" + (i + 1) + "in.txt");
            }
            if (output.length() > MAX_TEST_TEXT_CHARS) {
                tooBigNames.add("test" + (i + 1) + "out.txt");
            }
        }

        if (!tooBigNames.isEmpty()) {
            String names = String.join(", ", tooBigNames);
            log.warn(
                    "Code page add failed - tests too big: {}, pageId={}",
                    names,
                    pageId
            );
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Файл теста(ов) " + names + " слишком большой"
            );
        }
    }

    private void ensureCanEditCodeTasks(UUID courseId, UUID userId) {
        boolean allowed = courseMemberRepository
                .existsByIdCourseIdAndIdUserIdAndRoleIn(
                        courseId,
                        userId,
                        List.of(CourseMemberRole.OWNER, CourseMemberRole.ADMIN)
                );

        if (!allowed) {
            log.warn(
                    "Code page edit forbidden: courseId={}, userId={}",
                    courseId,
                    userId
            );
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Недостаточно прав для редактирования кодового задания"
            );
        }
    }
}
