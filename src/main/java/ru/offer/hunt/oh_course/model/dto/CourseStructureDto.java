package ru.offer.hunt.oh_course.model.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CourseStructureDto(
        UUID courseId,
        String title,
        Integer version,
        String status,
        OffsetDateTime updatedAt,
        List<LessonDto> lessons
) {

    public record LessonDto(
            UUID lessonId,
            String title,
            String description,
            Integer orderIndex,
            Integer durationMin,
            Boolean isDemo,
            List<PageDto> pages
    ) {}

    public record PageDto(
            UUID pageId,
            String title,
            String pageType,     // THEORY/TEST/CODE_TASK
            Integer sortOrder,
            MethodicalContentDto methodicalContent, // только для THEORY, иначе null
            List<QuestionDto> questions             // для TEST/CODE_TASK, для THEORY может быть пусто
    ) {}

    public record MethodicalContentDto(
            String markdown,
            String externalVideoUrl,
            OffsetDateTime updatedAt
    ) {}

    public record QuestionDto(
            UUID questionId,
            String type,         // SINGLE_CHOICE/MULTIPLE_CHOICE/TEXT_INPUT/CODE
            String text,
            String correctAnswer,
            Boolean useAiCheck,
            Integer points,
            Integer sortOrder,
            List<QuestionOptionDto> options,
            List<QuestionTestCaseDto> testCases
    ) {}

    public record QuestionOptionDto(
            UUID optionId,
            String label,
            Boolean isCorrect,
            Integer sortOrder
    ) {}

    public record QuestionTestCaseDto(
            UUID testCaseId,
            String inputData,
            String expectedOutput,
            Integer timeoutMs,
            Integer memoryLimitMb
    ) {}
}
