package ru.offer.hunt.oh_course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.LessonDto;
import ru.offer.hunt.oh_course.model.dto.LessonPageDto;
import ru.offer.hunt.oh_course.model.dto.LessonPageUpsertRequest;
import ru.offer.hunt.oh_course.model.dto.LessonUpsertRequest;
import ru.offer.hunt.oh_course.service.LessonPageService;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Страницы", description = "Управление контентными страницами внутри глав")
public class LessonPageController {
    private final LessonPageService lessonPageService;

    @PostMapping("/chapters/{chapterId}/lessons")
    @Operation(summary = "Добавить урок в главу", description = "Создает новый урок внутри указанной главы.")
    public ResponseEntity<LessonPageDto> addLessonToChapter(
            @PathVariable UUID chapterId,
            @Valid @RequestBody LessonPageUpsertRequest request
    ) {
        LessonPageDto createdLesson = lessonPageService.addLessonToChapter(chapterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLesson);
    }

    @PostMapping("/lessons/{lessonId}/pages")
    @Operation(summary = "Добавить страницу", description = "Добавляет новую страницу в указанный урок (главу)")
    public ResponseEntity<LessonPageDto> addLessonPage(
            @PathVariable UUID lessonId,
            @Valid @RequestBody LessonPageUpsertRequest request
    ) {
        LessonPageDto lessonPageDto = lessonPageService.addLessonPage(lessonId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonPageDto);
    }

    @PutMapping("/pages/{pageId}")
    @Operation(summary = "Обновить страницу")
    public ResponseEntity<LessonPageDto> updateLessonPage(
            @PathVariable UUID pageId,
            @Valid @RequestBody LessonPageUpsertRequest updateDto
    ) {
        LessonPageDto updatedLessonPage = lessonPageService.updateLessonPage(pageId, updateDto);
        return ResponseEntity.ok(updatedLessonPage);
    }

    @DeleteMapping("/pages/{pageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить страницу", description = "Удаляет страницу и весь связанный контент по ID страницы.")
    public void deleteLessonPage(
            @PathVariable UUID pageId
    ) {
        lessonPageService.deleteLessonPage(pageId);
    }
}
