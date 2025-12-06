package ru.offer.hunt.oh_course.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.offer.hunt.oh_course.model.dto.CourseDto;
import ru.offer.hunt.oh_course.model.dto.CourseUpsertRequest;
import ru.offer.hunt.oh_course.model.search.CourseFilter;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.CourseService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public List<CourseDto> getPublishedCourses(@Parameter(description = "Язык программирования")
                                               @RequestParam(value = "language", required = false)
                                               List<String> language,
                                               @Parameter(description = "Технологии")
                                               @RequestParam(value = "technologies", required = false)
                                               List<String> technologies,
                                               @Parameter(description = "Сложность")
                                               @RequestParam(value = "level", required = false)
                                               List<String> level,
                                               @Parameter(description = "Длительность")
                                               @RequestParam(value = "duration", required = false)
                                               List<Integer> duration,
                                               @Parameter(description = "Поисковый запрос")
                                               @RequestParam(value = "query", required = false)
                                               String query,
                                               @Parameter(description = "authorId")
                                               @RequestParam(value = "authorId", required = false)
                                               UUID authorId
    ) {
        log.debug("Get courses request");

        CourseFilter filter = new CourseFilter(authorId, language, technologies, level, duration, query);
        return courseService.getPublishedCourses(filter);
    }

    @GetMapping("/{slug}")
    public CourseDto getPublishedCourseBySlug(
            @PathVariable String slug,
            @RequestParam(value = "inviteCode", required = false) String inviteCode
    ) {
        log.debug("Get course by slug request: slug='{}'", slug);
        return courseService.getPublishedCourseBySlug(slug, inviteCode);
    }

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
