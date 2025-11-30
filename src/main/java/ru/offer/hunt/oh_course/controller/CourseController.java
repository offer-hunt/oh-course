package ru.offer.hunt.oh_course.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.CourseDto;
import ru.offer.hunt.oh_course.model.dto.CourseUpsertRequest;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.CourseService;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDto createCourse(@Valid @RequestBody CourseUpsertRequest request,
                                  JwtAuthenticationToken authentication) {

        UUID authorId = SecurityUtils.getUserId(authentication);

        log.debug("Create course request: title='{}', authorId={}",
                request.getTitle(), authorId);

        return courseService.createCourse(authorId, request);
    }

    @PostMapping("/{courseId}/publish")
    public CourseDto publishCourse(
            @PathVariable UUID courseId,
            JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        return courseService.publishCourse(courseId, userId);
    }
}
