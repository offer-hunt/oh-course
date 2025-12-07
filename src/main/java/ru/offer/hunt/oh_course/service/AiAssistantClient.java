package ru.offer.hunt.oh_course.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.offer.hunt.oh_course.model.dto.CodeTestCaseDto;
import ru.offer.hunt.oh_course.model.dto.CodeTaskGenerationResponse;
import ru.offer.hunt.oh_course.model.dto.GeneratedQuestionDto;
import ru.offer.hunt.oh_course.model.dto.TestGenerationResponse;
import ru.offer.hunt.oh_course.model.dto.TextEnhancementRequest;
import ru.offer.hunt.oh_course.model.dto.TextEnhancementResponse;
import ru.offer.hunt.oh_course.model.enums.CodeLanguage;
import ru.offer.hunt.oh_course.model.enums.QuestionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stub implementation of AI Assistant client.
 * In production, this would make HTTP calls to an external AI service.
 */
@Service
@Slf4j
public class AiAssistantClient {

    /**
     * Улучшение текста с помощью AI
     */
    public Optional<TextEnhancementResponse> enhanceText(String text, TextEnhancementRequest.TextEnhancementAction action) {
        if (text == null || text.isBlank()) {
            log.warn("AiAssistantClient: empty text provided for enhancement");
            return Optional.empty();
        }

        // Stub implementation - в реальности здесь будет HTTP-запрос к AI-сервису
        String enhancedText = switch (action) {
            case SIMPLIFY -> "Упрощенная версия: " + text;
            case ACADEMIC -> "Академическая версия: " + text;
            case GRAMMAR -> "Исправленная грамматика: " + text;
            case EXPAND -> "Расширенная мысль: " + text;
            case EXAMPLE -> "Пример: " + text;
        };

        log.info("AiAssistantClient: text enhanced with action={}, originalLength={}, enhancedLength={}",
                action, text.length(), enhancedText.length());

        return Optional.of(new TextEnhancementResponse(enhancedText));
    }

    /**
     * Генерация тестовых вопросов
     */
    public Optional<TestGenerationResponse> generateTestQuestions(
            String context,
            String topic,
            QuestionType questionType,
            Integer questionCount,
            String difficulty
    ) {
        if (context == null || context.isBlank()) {
            log.warn("AiAssistantClient: empty context provided for test generation");
            return Optional.empty();
        }

        if (context.length() < 100) {
            // Недостаточно контекста
            log.warn("AiAssistantClient: insufficient context for test generation, length={}", context.length());
            return Optional.empty();
        }

        // Stub implementation - генерируем примеры вопросов
        List<GeneratedQuestionDto> questions = new ArrayList<>();
        for (int i = 1; i <= questionCount; i++) {
            questions.add(new GeneratedQuestionDto(
                    "Сгенерированный вопрос " + i + " по теме: " + (topic != null ? topic : "урок"),
                    List.of("Вариант 1", "Вариант 2", "Вариант 3", "Вариант 4"),
                    List.of(0) // правильный ответ - первый вариант
            ));
        }

        log.info("AiAssistantClient: test questions generated, count={}, type={}, difficulty={}",
                questions.size(), questionType, difficulty);

        return Optional.of(new TestGenerationResponse(questions));
    }

    /**
     * Генерация кодового задания
     */
    public Optional<CodeTaskGenerationResponse> generateCodeTask(
            String topic,
            CodeLanguage language,
            String difficulty,
            String requirements
    ) {
        if (topic == null || topic.isBlank()) {
            log.warn("AiAssistantClient: empty topic provided for code task generation");
            return Optional.empty();
        }

        // Проверка на противоречивые требования (stub)
        if (language == CodeLanguage.PYTHON && requirements != null
                && requirements.contains("LINQ")) {
            log.warn("AiAssistantClient: invalid parameters - Python cannot use LINQ");
            return Optional.empty();
        }

        // Stub implementation
        String description = String.format(
                "Напишите функцию для: %s. Язык: %s. Уровень сложности: %s",
                topic,
                language,
                difficulty != null ? difficulty : "средний"
        );

        String exampleSolution = "def solution():\n    # Ваше решение здесь\n    pass";

        List<CodeTestCaseDto> testCases = List.of(
                new CodeTestCaseDto("input1", "output1"),
                new CodeTestCaseDto("input2", "output2")
        );

        log.info("AiAssistantClient: code task generated, language={}, difficulty={}",
                language, difficulty);

        return Optional.of(new CodeTaskGenerationResponse(description, exampleSolution, testCases));
    }
}

