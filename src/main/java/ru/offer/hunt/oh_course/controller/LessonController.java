package ru.offer.hunt.oh_course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.LessonDto;
import ru.offer.hunt.oh_course.model.dto.LessonUpsertRequest;
import ru.offer.hunt.oh_course.service.LessonService;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Главы (Lessons)", description = "Управление главами курсов")
public class LessonController {
    private final LessonService lessonService;

    @PostMapping("/courses/{courseId}/lessons")
    @Operation(summary = "Создать новую главу", description = "Создает новую главу внутри курса по ID.")
    public ResponseEntity<LessonDto> addLesson(
            @PathVariable UUID courseId,
            @Valid @RequestBody LessonUpsertRequest createDto
    ) {
        LessonDto created = lessonService.createLesson(courseId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/lessons/{lessonId}")
    @Operation(summary = "Обновить главу", description = "Обновляет название и описание существующей главы.")
    public ResponseEntity<LessonDto> updateLesson(
            @PathVariable UUID lessonId,
            @Valid @RequestBody LessonUpsertRequest updateDto
    ) {
        LessonDto updatedLesson = lessonService.updateLesson(lessonId, updateDto);
        return ResponseEntity.ok(updatedLesson);
    }
}
