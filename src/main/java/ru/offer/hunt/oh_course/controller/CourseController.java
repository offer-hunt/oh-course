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
import ru.offer.hunt.oh_course.model.dto.CourseDto;
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
}
