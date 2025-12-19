package ru.offer.hunt.oh_course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.LessonDto;
import ru.offer.hunt.oh_course.model.dto.LessonPageDto;
import ru.offer.hunt.oh_course.model.dto.LessonPageUpsertRequest;
import ru.offer.hunt.oh_course.model.dto.LessonUpsertRequest;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.LessonPageService;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Страницы", description = "Управление контентными страницами внутри глав")
public class LessonPageController {
    private final LessonPageService lessonPageService;

    @PostMapping("/chapters/{chapterId}/lessons")
    @Operation(summary = "Добавить урок в главу")
    public ResponseEntity<LessonPageDto> addLessonToChapter(
            @PathVariable UUID chapterId,
            @Valid @RequestBody LessonPageUpsertRequest request,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lessonPageService.addLessonToChapter(chapterId, userId, request));
    }

    @PutMapping("/pages/{pageId}")
    @Operation(summary = "Обновить страницу")
    public ResponseEntity<LessonPageDto> updateLessonPage(
            @PathVariable UUID pageId,
            @Valid @RequestBody LessonPageUpsertRequest updateDto,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        return ResponseEntity.ok(lessonPageService.updateLessonPage(pageId, userId, updateDto));
    }

    @DeleteMapping("/pages/{pageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить страницу")
    public void deleteLessonPage(
            @PathVariable UUID pageId,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        lessonPageService.deleteLessonPage(pageId, userId);
    }
}
