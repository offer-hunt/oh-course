package ru.offer.hunt.oh_course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.ContentVersionDetailsDto;
import ru.offer.hunt.oh_course.model.dto.ContentVersionSummaryDto;
import ru.offer.hunt.oh_course.model.dto.VersionSaveRequest;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.ContentVersionService;

@RestController
@RequestMapping("/api/lessons/{lessonId}/versions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Lesson versions", description = "Версионирование настроек урока (снимки и откаты)")
public class LessonVersionController {

    private final ContentVersionService contentVersionService;

    @Operation(
            summary = "Сохранить версию урока",
            description = """
                    Создаёт версию (снимок) основных полей урока \
                    (title, description, orderIndex, durationMin) с произвольным комментарием."""
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContentVersionDetailsDto saveLessonVersion(@PathVariable UUID lessonId,
                                                      @Valid @RequestBody(required = false)
                                                      VersionSaveRequest request,
                                                      JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        return contentVersionService.saveLessonVersion(lessonId, userId, request);
    }

    @Operation(
            summary = "Получить список версий урока",
            description = "Возвращает список сохранённых версий конкретного урока в порядке от новых к старым."
    )
    @GetMapping
    public List<ContentVersionSummaryDto> listLessonVersions(@PathVariable UUID lessonId,
                                                             JwtAuthenticationToken authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        return contentVersionService.getLessonVersions(lessonId, userId);
    }

    @Operation(
            summary = "Получить сохранённую версию урока",
            description = "Возвращает подробную информацию по конкретной версии урока."
    )
    @GetMapping("/{versionId}")
    public ContentVersionDetailsDto getLessonVersion(@PathVariable UUID lessonId,
                                                     @PathVariable UUID versionId,
                                                     JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        return contentVersionService.getLessonVersion(lessonId, versionId, userId);
    }

    @Operation(
            summary = "Восстановить урок из сохранённой версии",
            description = "Применяет сохранённую версию к уроку, перезаписывая его основные поля."
    )
    @PostMapping("/{versionId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreLessonVersion(@PathVariable UUID lessonId,
                                     @PathVariable UUID versionId,
                                     JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        contentVersionService.restoreLessonVersion(lessonId, versionId, userId);
    }
}
