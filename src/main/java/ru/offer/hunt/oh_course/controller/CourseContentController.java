package ru.offer.hunt.oh_course.controller;

import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.offer.hunt.oh_course.model.dto.CourseOutlineDto;
import ru.offer.hunt.oh_course.model.dto.LessonPageDto;
import ru.offer.hunt.oh_course.model.dto.LessonPageShortDto;
import ru.offer.hunt.oh_course.model.dto.LessonPageUpsertRequest;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentDto;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentUpsertRequest;
import ru.offer.hunt.oh_course.model.dto.PageViewDto;
import ru.offer.hunt.oh_course.model.dto.QuestionDto;
import ru.offer.hunt.oh_course.model.dto.QuestionOptionDto;
import ru.offer.hunt.oh_course.model.dto.QuestionOptionUpsertRequest;
import ru.offer.hunt.oh_course.model.dto.QuestionUpsertRequest;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.CourseContentService;
import ru.offer.hunt.oh_course.service.LessonService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class CourseContentController {

    private final CourseContentService courseContentService;
    private final LessonService lessonService;

    // 1) Outline: курс + уроки (без страниц)
    @GetMapping("/courses/{slug}/outline")
    public CourseOutlineDto getCourseOutline(
            @PathVariable String slug,
            @Parameter(description = "Invite code для PRIVATE_LINK курсов")
            @RequestParam(value = "inviteCode", required = false) String inviteCode
    ) {
        log.debug("Get course outline: slug={}", slug);
        return courseContentService.getCourseOutline(slug, inviteCode);
    }

    // 2) Страницы урока (только демо)
    @GetMapping("/lessons/{lessonId}/pages")
    public List<LessonPageShortDto> getDemoLessonPages(
            @PathVariable UUID lessonId,
            @RequestParam(value = "inviteCode", required = false) String inviteCode
    ) {
        log.debug("Get demo lesson pages: lessonId={}", lessonId);
        return courseContentService.getDemoLessonPages(lessonId, inviteCode);
    }

    // 3) Просмотр страницы (read-only + без секретов)
    @GetMapping("/pages/{pageId}/view")
    public PageViewDto getDemoPageView(
            @PathVariable UUID pageId,
            @RequestParam(value = "inviteCode", required = false) String inviteCode
    ) {
        log.debug("Get demo page view: pageId={}", pageId);
        return courseContentService.getDemoPageView(pageId, inviteCode);
    }

    // 4) Создание страниц урока
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

    // 5.1 Upsert THEORY контента
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

    // 5.2 Добавить вопрос на TEST-страницу
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

    // 5.3 Добавить опцию к вопросу
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
}
