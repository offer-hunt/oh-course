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
import ru.offer.hunt.oh_course.model.dto.LessonCreateRequest;
import ru.offer.hunt.oh_course.model.dto.LessonDto;
import ru.offer.hunt.oh_course.model.dto.LessonUpsertRequest;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.LessonService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Lessons", description = "Управление уроками внутри курса (авторские операции)")
public class LessonController {
    private final LessonService lessonService;

    @Operation(
            summary = "Создать урок в курсе",
            description = """
                    Добавляет новый урок в указанный курс. \
                    Доступно только для участников курса с ролью OWNER/ADMIN. \
                    Урок может быть помечен как demo (доступен в публичной части)."""
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LessonDto createLesson(@PathVariable UUID courseId,
                                  @Valid @RequestBody LessonCreateRequest req,
                                  JwtAuthenticationToken authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        log.debug("Create lesson: courseId={}, userId={}", courseId, userId);
        return lessonService.createLesson(courseId, userId, req);
    }

    @Operation(
            summary = "Список уроков курса (только для авторов)",
            description = """
                    Возвращает все уроки указанного курса в порядке orderIndex. \
                    Используется в редакторе курсов; доступен только участникам с ролью OWNER/ADMIN."""
    )
    @GetMapping
    public List<LessonDto> listLessons(@PathVariable UUID courseId,
                                       JwtAuthenticationToken authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        return lessonService.listLessons(courseId, userId);
    }

    @PutMapping("/{lessonId}")
    @Operation(summary = "Обновить главу", description = "Обновляет название и описание существующей главы.")
    public ResponseEntity<LessonDto> updateLesson(
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId,
            @Valid @RequestBody LessonUpsertRequest updateDto,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        LessonDto updated = lessonService.updateLesson(lessonId, courseId, userId, updateDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{lessonId}")
    @Operation(summary = "Удалить главу", description = "Удаляет главу и все связанные страницы.")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);

        try {
            lessonService.deleteLesson(lessonId, courseId, userId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
