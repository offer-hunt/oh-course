package ru.offer.hunt.oh_course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.*;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.CourseContentService;
import ru.offer.hunt.oh_course.service.LessonService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
@Tag(name = "Course content", description = "Публичный демо-контент курса и авторские операции с контентом")
public class CourseContentController {

    private final CourseContentService courseContentService;
    private final LessonService lessonService;

    @Operation(
            summary = "Получить структуру курса (outline) для публичной части",
            description = """
                    возвращает список уроков опубликованного курса \
                    с пометкой demo/locked. Для курсов PRIVATE_LINK требуется корректный inviteCode."""
    )
    @GetMapping("/courses/{slug}/outline")
    public CourseOutlineDto getCourseOutline(
            @PathVariable String slug,
            @Parameter(description = "Invite code для PRIVATE_LINK курсов")
            @RequestParam(value = "inviteCode", required = false) String inviteCode
    ) {
        log.debug("Get course outline: slug={}", slug);
        return courseContentService.getCourseOutline(slug, inviteCode);
    }
    @Operation(
            summary = "Получить список страниц демо-урока",
            description = """
                    Возвращает список страниц только для урока, помеченного как demo. \
                    Используется для предпросмотра содержания курса в публичной части."""
    )
    @GetMapping("/lessons/{lessonId}/pages")
    public List<LessonPageShortDto> getDemoLessonPages(
            @PathVariable UUID lessonId,
            @RequestParam(value = "inviteCode", required = false) String inviteCode
    ) {
        log.debug("Get demo lesson pages: lessonId={}", lessonId);
        return courseContentService.getDemoLessonPages(lessonId, inviteCode);
    }

    @Operation(
            summary = "Просмотр содержимого демо-страницы",
            description = """
                    Сценарий демо-страницы курса: \
                    возвращает теоретический контент или вопросы без правильных ответов, \
                    только в режиме read-only, плюс CTA на запись на курс."""
    )
    @GetMapping("/pages/{pageId}/view")
    public PageViewDto getDemoPageView(
            @PathVariable UUID pageId,
            @RequestParam(value = "inviteCode", required = false) String inviteCode
    ) {
        log.debug("Get demo page view: pageId={}", pageId);
        return courseContentService.getDemoPageView(pageId, inviteCode);
    }

    @Operation(
            summary = "Создать страницу урока",
            description = """
                    Авторский сценарий: добавляет новую страницу (THEORY/TEST/CODE_TASK) в указанный урок. \
                    Доступно только OWNER/ADMIN курса; запрещено для архивированных курсов."""
    )
    @PostMapping("/lessons/{lessonId}/pages")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonPageDto createLessonPage(
            @PathVariable UUID lessonId,
            @Valid @RequestBody LessonPageUpsertRequest req,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        log.debug("Create lesson page: lessonId={}, userId={}", lessonId, userId);
        return lessonService.createLessonPage(lessonId, userId, req);
    }

    @Operation(
            summary = "Создать/обновить методический контент THEORY-страницы",
            description = """
                    Авторский сценарий: создаёт или обновляет текстовый/видео контент \
                    для страницы с типом THEORY. Для других типов страниц возвращает ошибку."""
    )
    @PostMapping("/pages/{pageId}/methodical")
    public ResponseEntity<MethodicalPageContentDto> upsertMethodical(
            @PathVariable UUID pageId,
            @Valid @RequestBody MethodicalPageContentUpsertRequest req,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        var res = lessonService.upsertMethodical(pageId, userId, req);
        return ResponseEntity.status(res.created() ? HttpStatus.CREATED : HttpStatus.OK).body(res.dto());
    }

    @Operation(
            summary = "Добавить вопрос на TEST-страницу",
            description = """
                    Авторский сценарий: создаёт вопрос для TEST-страницы урока \
                    (single choice / multiple choice / text / code). \
                    Для страниц другого типа возвращает ошибку."""
    )
    @PostMapping("/pages/{pageId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionDto createQuestion(
            @PathVariable UUID pageId,
            @Valid @RequestBody QuestionUpsertRequest req,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        return lessonService.createQuestion(pageId, userId, req);
    }

    @Operation(
            summary = "Добавить вариант ответа к вопросу",
            description = """
                    Авторский сценарий: добавляет опцию ответа к choice-вопросу \
                    (SINGLE_CHOICE или MULTIPLE_CHOICE). \
                    Для вопросов других типов возвращает ошибку."""
    )
    @PostMapping("/questions/{questionId}/options")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionOptionDto createQuestionOption(
            @PathVariable UUID questionId,
            @Valid @RequestBody QuestionOptionUpsertRequest req,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        return lessonService.createQuestionOption(questionId, userId, req);
    }


    @Operation(
            summary = "Получить структуру курса для расчёта прогресса",
            description = """
                Возвращает структуру курса без контента:
                уроки → страницы → идентификаторы вопросов.
                Используется Learning-сервисом для расчёта прогресса.
            """
    )
    @GetMapping("/v1/courses/{courseId}/structure")
    public CourseOutlineLiteDto getCourseStructure(
            @PathVariable UUID courseId
    ) {
        return courseContentService.getCourseStructureLite(courseId);
    }
}
