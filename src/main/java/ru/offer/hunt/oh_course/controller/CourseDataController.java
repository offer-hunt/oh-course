package ru.offer.hunt.oh_course.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.offer.hunt.oh_course.model.dto.CourseGetDto;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.CourseDataService;

import java.util.UUID;

@RestController
@RequestMapping("/api/coursesDataController")
@RequiredArgsConstructor
@Slf4j
public class CourseDataController {
    private CourseDataService courseDataService;

    @GetMapping("/get/{courseId}")
    public CourseGetDto getCourse(
            @PathVariable("courseId") UUID courseId,
            JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        return courseDataService.getCourseDto(courseId, userId);
    }
}
