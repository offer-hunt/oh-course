package ru.offer.hunt.oh_course.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.LessonCreateRequest;
import ru.offer.hunt.oh_course.model.dto.LessonDto;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.LessonService;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
@RequiredArgsConstructor
@Slf4j
public class LessonController {

    private final LessonService lessonService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LessonDto createLesson(@PathVariable UUID courseId,
                                  @Valid @RequestBody LessonCreateRequest req,
                                  JwtAuthenticationToken authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        log.debug("Create lesson: courseId={}, userId={}", courseId, userId);
        return lessonService.createLesson(courseId, userId, req);
    }

    // чисто для удобства проверок в swagger
    @GetMapping
    public List<LessonDto> listLessons(@PathVariable UUID courseId,
                                       JwtAuthenticationToken authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        return lessonService.listLessons(courseId, userId);
    }
}
