package ru.offer.hunt.oh_course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.AddTagsRequest;
import ru.offer.hunt.oh_course.model.dto.CourseDto;
import ru.offer.hunt.oh_course.model.dto.CoursePreviewDto;
import ru.offer.hunt.oh_course.model.dto.CourseUpsertRequest;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.CourseService;

import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Курсы", description = "Управление жизненным циклом курсов")
public class CourseController {
    private final CourseService courseService;

    @PostMapping("/{courseId}/versions/draft")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать новую версию", description = "Копирует структуру опубликованного курса в новый черновик с инкрементом версии.")
    public ResponseEntity<CourseDto> createDraftFromPublished(
            @PathVariable UUID courseId,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        log.info("Request to create draft version from courseId={}, userId={}", courseId, userId);

        CourseDto draftCourse = courseService.createDraftFromPublished(courseId, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(draftCourse);
    }

    @PostMapping("/{draftCourseId}/publish")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Опубликовать черновик курса", description = "Меняет статус черновика на PUBLISHED, архивирует старую версию и обновляет статистику.")
    public ResponseEntity<CourseDto> publishCourseDraft(
            @PathVariable UUID draftCourseId,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        log.info("Request to publish courseId={}, userId={}", draftCourseId, userId);

        CourseDto publishedCourse = courseService.publishDraftCourse(draftCourseId, userId);

        return ResponseEntity.ok(publishedCourse);
    }

    @PostMapping("/{courseId}/archive")
    @Operation(summary = "Архивировать курс", description = "Переводит опубликованный курс в статус ARCHIVED.")
    public ResponseEntity<Void> archiveCourse(
            @PathVariable UUID courseId,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        courseService.archiveCourse(courseId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{courseId}")
    @Operation(summary = "Удалить курс", description = "Полностью удаляет курс и его содержимое.")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable UUID courseId,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        courseService.deleteCourse(courseId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/tags")
    @Operation(summary = "Добавить теги", description = "Добавляет теги к курсу (макс. 10).")
    public ResponseEntity<Void> addTags(
            @PathVariable UUID courseId,
            @RequestBody AddTagsRequest request,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        courseService.addTags(courseId, userId, request.getTagIds());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{courseId}/tags/{tagId}")
    @Operation(summary = "Удалить тег", description = "Удаляет тег у курса (мин. 1 должен остаться).")
    public ResponseEntity<Void> removeTag(
            @PathVariable UUID courseId,
            @PathVariable UUID tagId,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        courseService.removeTag(courseId, userId, tagId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{courseId}/preview")
    @Operation(summary = "Предпросмотр курса", description = "Возвращает структуру курса для предпросмотра (только для Draft).")
    public ResponseEntity<CoursePreviewDto> previewCourse(
            @PathVariable UUID courseId,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        CoursePreviewDto preview = courseService.getCoursePreview(courseId, userId);
        return ResponseEntity.ok(preview);
    }
}
