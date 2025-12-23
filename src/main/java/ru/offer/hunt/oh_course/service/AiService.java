package ru.offer.hunt.oh_course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.CodeTaskGenerationRequest;
import ru.offer.hunt.oh_course.model.dto.CodeTaskGenerationResponse;
import ru.offer.hunt.oh_course.model.dto.GeneratedQuestionDto;
import ru.offer.hunt.oh_course.model.dto.TestGenerationRequest;
import ru.offer.hunt.oh_course.model.dto.TestGenerationResponse;
import ru.offer.hunt.oh_course.model.dto.TextEnhancementRequest;
import ru.offer.hunt.oh_course.model.dto.TextEnhancementResponse;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.entity.LessonPage;
import ru.offer.hunt.oh_course.model.entity.MethodicalPageContent;
import ru.offer.hunt.oh_course.model.enums.CourseMemberRole;
import ru.offer.hunt.oh_course.model.enums.PageType;
import ru.offer.hunt.oh_course.model.repository.CourseMemberRepository;
import ru.offer.hunt.oh_course.model.repository.LessonPageRepository;
import ru.offer.hunt.oh_course.model.repository.LessonRepository;
import ru.offer.hunt.oh_course.model.repository.MethodicalPageContentRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final AiAssistantClient aiAssistantClient;
    private final LessonRepository lessonRepository;
    private final LessonPageRepository lessonPageRepository;
    private final MethodicalPageContentRepository methodicalPageContentRepository;
    private final CourseMemberRepository courseMemberRepository;

    /**
     * Сценарий 17: Улучшение текста урока с помощью AI
     */
    @Transactional(readOnly = true)
    public TextEnhancementResponse enhanceText(UUID pageId, UUID userId, TextEnhancementRequest request) {
        LessonPage page = lessonPageRepository.findById(pageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена"));

        UUID courseId = page.getLesson().getCourse().getId();
        ensureCourseAdmin(courseId, userId);

        if (page.getPageType() != PageType.THEORY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Улучшение текста доступно только для страниц с методическими материалами");
        }

        MethodicalPageContent content = methodicalPageContentRepository.findById(pageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Контент страницы не найден"));

        try {
            var response = aiAssistantClient.enhanceText(request.getText(), request.getAction());

            if (response.isEmpty()) {
                // Пустой или некорректный ответ
                log.warn("AI invalid response: pageId={}, lessonId={}, action={}",
                        pageId, page.getLesson().getId(), request.getAction());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Не удалось обработать запрос. Попробуйте изменить формулировку");
            }

            // Проверка на валидность ответа
            if (response.get().getEnhancedText() == null || response.get().getEnhancedText().isBlank()) {
                log.warn("AI invalid response: pageId={}, lessonId={}, action={}",
                        pageId, page.getLesson().getId(), request.getAction());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Не удалось обработать запрос. Попробуйте изменить формулировку");
            }

            // Логирование успешной генерации (17.6)
            log.info("AI text enhancement success: lessonId={}, action={}",
                    page.getLesson().getId(), request.getAction());

            return response.get();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            // Обработка ошибок AI-сервиса (таймаут, 5xx и т.д.) (17.7)
            log.warn("AI service unavailable: pageId={}, lessonId={}, error={}",
                    pageId, page.getLesson().getId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI-ассистент временно недоступен. Пожалуйста, попробуйте позже");
        }
    }

    /**
     * Сценарий 18: Генерация тестовых вопросов с помощью AI
     */
    @Transactional(readOnly = true)
    public TestGenerationResponse generateTestQuestions(UUID lessonId, UUID userId, TestGenerationRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Урок не найден"));

        UUID courseId = lesson.getCourse().getId();
        ensureCourseAdmin(courseId, userId);

        // Проверяем, что есть хотя бы одна TEST-страница
        List<LessonPage> testPages = lessonPageRepository.findByLessonIdOrderBySortOrderAsc(lessonId).stream()
                .filter(p -> p.getPageType() == PageType.TEST)
                .toList();

        if (testPages.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "В уроке должна быть хотя бы одна страница с тестовыми заданиями");
        }

        // Собираем контекст из методических страниц урока
        String context = buildLessonContext(lessonId);

        if (context.length() < 100) {
            // Логирование недостаточного контекста (18.7)
            log.warn("AI test generation insufficient context: lessonId={}, contextLength={}",
                    lessonId, context.length());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Недостаточно контекста для генерации вопросов. Добавьте больше учебного материала в урок");
        }

        try {
            var response = aiAssistantClient.generateTestQuestions(
                    context,
                    request.getTopic(),
                    request.getQuestionType(),
                    request.getQuestionCount(),
                    request.getDifficulty());

            if (response.isEmpty()) {
                // AI не смог сгенерировать вопросы (18.7)
                log.warn("AI test generation insufficient context: lessonId={}", lessonId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Недостаточно контекста для генерации вопросов. Добавьте больше учебного материала в урок");
            }

            // Логирование успешной генерации (18.6)
            log.info("AI test generation success: lessonId={}, questionCount={}",
                    lessonId, response.get().getQuestions().size());

            return response.get();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.warn("AI service unavailable: lessonId={}, error={}", lessonId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI-ассистент временно недоступен. Пожалуйста, попробуйте позже");
        }
    }

    /**
     * Сценарий 19: Генерация кодового задания с помощью AI
     */
    @Transactional(readOnly = true)
    public CodeTaskGenerationResponse generateCodeTask(UUID lessonId, UUID userId, CodeTaskGenerationRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Урок не найден"));

        UUID courseId = lesson.getCourse().getId();
        ensureCourseAdmin(courseId, userId);

        // Проверяем, что есть хотя бы одна CODE_TASK-страница
        List<LessonPage> codePages = lessonPageRepository.findByLessonIdOrderBySortOrderAsc(lessonId).stream()
                .filter(p -> p.getPageType() == PageType.CODE_TASK)
                .toList();

        if (codePages.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "В уроке должна быть хотя бы одна страница с кодовыми заданиями");
        }

        try {
            var response = aiAssistantClient.generateCodeTask(
                    request.getTopic(),
                    request.getLanguage(),
                    request.getDifficulty(),
                    request.getRequirements());

            if (response.isEmpty()) {
                // Некорректные параметры генерации (19.7)
                log.warn("AI code task generation invalid parameters: lessonId={}, topic={}, language={}",
                        lessonId, request.getTopic(), request.getLanguage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Невозможно сгенерировать задание с указанными параметрами. Проверьте корректность введенных данных");
            }

            // Логирование успешной генерации (19.6)
            log.info("AI code task generation success: lessonId={}, language={}",
                    lessonId, request.getLanguage());

            return response.get();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.warn("AI service unavailable: lessonId={}, error={}", lessonId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI-ассистент временно недоступен. Пожалуйста, попробуйте позже");
        }
    }

    private void ensureCourseAdmin(UUID courseId, UUID userId) {
        boolean allowed = courseMemberRepository.existsByIdCourseIdAndIdUserIdAndRoleIn(
                courseId,
                userId,
                List.of(CourseMemberRole.OWNER, CourseMemberRole.ADMIN));

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Недостаточно прав для управления курсом");
        }
    }

    private String buildLessonContext(UUID lessonId) {
        List<LessonPage> pages = lessonPageRepository.findByLessonIdOrderBySortOrderAsc(lessonId);
        StringBuilder context = new StringBuilder();

        for (LessonPage page : pages) {
            if (page.getPageType() == PageType.THEORY) {
                methodicalPageContentRepository.findById(page.getId())
                        .ifPresent(content -> {
                            if (content.getMarkdown() != null) {
                                context.append(content.getMarkdown()).append("\n\n");
                            }
                        });
            }
        }

        return context.toString().trim();
    }
}

