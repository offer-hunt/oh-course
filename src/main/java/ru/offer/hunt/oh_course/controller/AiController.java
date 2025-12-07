package ru.offer.hunt.oh_course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.CodeTaskGenerationRequest;
import ru.offer.hunt.oh_course.model.dto.CodeTaskGenerationResponse;
import ru.offer.hunt.oh_course.model.dto.TestGenerationRequest;
import ru.offer.hunt.oh_course.model.dto.TestGenerationResponse;
import ru.offer.hunt.oh_course.model.dto.TextEnhancementRequest;
import ru.offer.hunt.oh_course.model.dto.TextEnhancementResponse;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.AiService;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Assistant", description = "AI-функции для улучшения контента курсов")
public class AiController {

    private final AiService aiService;

    @Operation(
            summary = "Улучшение текста урока с помощью AI",
            description = """
                    Сценарий 17: Пользователь выделяет фрагмент текста и выбирает действие для улучшения. \
                    Доступно только для страниц с методическими материалами (THEORY). \
                    Доступно только для OWNER/ADMIN курса."""
    )
    @PostMapping("/pages/{pageId}/ai/enhance-text")
    public TextEnhancementResponse enhanceText(
            @PathVariable UUID pageId,
            @Valid @RequestBody TextEnhancementRequest request,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        log.debug("AI text enhancement request: pageId={}, userId={}, action={}", pageId, userId, request.getAction());
        return aiService.enhanceText(pageId, userId, request);
    }

    @Operation(
            summary = "Генерация тестовых вопросов с помощью AI",
            description = """
                    Сценарий 18: Генерация тестовых вопросов на основе контента урока. \
                    Требуется наличие хотя бы одной TEST-страницы в уроке. \
                    Доступно только для OWNER/ADMIN курса."""
    )
    @PostMapping("/lessons/{lessonId}/ai/generate-test")
    public TestGenerationResponse generateTestQuestions(
            @PathVariable UUID lessonId,
            @Valid @RequestBody TestGenerationRequest request,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        log.debug("AI test generation request: lessonId={}, userId={}, questionCount={}",
                lessonId, userId, request.getQuestionCount());
        return aiService.generateTestQuestions(lessonId, userId, request);
    }

    @Operation(
            summary = "Генерация кодового задания с помощью AI",
            description = """
                    Сценарий 19: Генерация кодового задания с указанными параметрами. \
                    Требуется наличие хотя бы одной CODE_TASK-страницы в уроке. \
                    Доступно только для OWNER/ADMIN курса."""
    )
    @PostMapping("/lessons/{lessonId}/ai/generate-code-task")
    public CodeTaskGenerationResponse generateCodeTask(
            @PathVariable UUID lessonId,
            @Valid @RequestBody CodeTaskGenerationRequest request,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        log.debug("AI code task generation request: lessonId={}, userId={}, language={}",
                lessonId, userId, request.getLanguage());
        return aiService.generateCodeTask(lessonId, userId, request);
    }
}

