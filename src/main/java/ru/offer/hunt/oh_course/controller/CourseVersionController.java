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
@RequestMapping("/api/courses/{courseId}/versions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Course versions", description = "Версионирование настроек курса (снимки и откаты)")
public class CourseVersionController {

    private final ContentVersionService contentVersionService;

    @Operation(
            summary = "Сохранить версию курса",
            description = """
                    Создаёт новую версию (снимок) текущего состояния полей курса \
                    (title, description, accessType и т.д.) с произвольным комментарием."""
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContentVersionDetailsDto saveCourseVersion(@PathVariable UUID courseId,
                                                      @Valid @RequestBody(required = false)
                                                      VersionSaveRequest request,
                                                      JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        return contentVersionService.saveCourseVersion(courseId, userId, request);
    }

    @Operation(
            summary = "Получить список версий курса",
            description = "Возвращает список сохранённых версий настроек курса в порядке от новых к старым."
    )
    @GetMapping
    public List<ContentVersionSummaryDto> listCourseVersions(@PathVariable UUID courseId,
                                                             JwtAuthenticationToken authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        return contentVersionService.getCourseVersions(courseId, userId);
    }

    @Operation(
            summary = "Получить сохранённую версию курса",
            description = "Возвращает подробную информацию по конкретной версии (payload в JSON и метаданные)."
    )
    @GetMapping("/{versionId}")
    public ContentVersionDetailsDto getCourseVersion(@PathVariable UUID courseId,
                                                     @PathVariable UUID versionId,
                                                     JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        return contentVersionService.getCourseVersion(courseId, versionId, userId);
    }

    @Operation(
            summary = "Восстановить курс из сохранённой версии",
            description = """
                    Применяет сохранённую версию к текущему курсу (перезаписывает поля курса значениями из payload). \
                    Действие необратимо с точки зрения текущего состояния (но можно сохранить ещё одну версию)."""
    )
    @PostMapping("/{versionId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreCourseVersion(@PathVariable UUID courseId,
                                     @PathVariable UUID versionId,
                                     JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        contentVersionService.restoreCourseVersion(courseId, versionId, userId);
    }
}
