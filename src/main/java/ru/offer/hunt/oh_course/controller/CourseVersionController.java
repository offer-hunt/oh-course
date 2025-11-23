package ru.offer.hunt.oh_course.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.ContentVersionDetailsDto;
import ru.offer.hunt.oh_course.model.dto.ContentVersionSummaryDto;
import ru.offer.hunt.oh_course.model.dto.VersionSaveRequest;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.ContentVersionService;

@RestController
@RequestMapping("/api/courses/{courseId}/versions")
@RequiredArgsConstructor
@Slf4j
public class CourseVersionController {

    private final ContentVersionService contentVersionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContentVersionDetailsDto saveCourseVersion(@PathVariable UUID courseId,
                                                      @Valid @RequestBody(required = false)
                                                      VersionSaveRequest request,
                                                      JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        return contentVersionService.saveCourseVersion(courseId, userId, request);
    }

    @GetMapping
    public List<ContentVersionSummaryDto> listCourseVersions(@PathVariable UUID courseId,
                                                             JwtAuthenticationToken authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        return contentVersionService.getCourseVersions(courseId, userId);
    }

    @GetMapping("/{versionId}")
    public ContentVersionDetailsDto getCourseVersion(@PathVariable UUID courseId,
                                                     @PathVariable UUID versionId,
                                                     JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        return contentVersionService.getCourseVersion(courseId, versionId, userId);
    }

    @PostMapping("/{versionId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreCourseVersion(@PathVariable UUID courseId,
                                     @PathVariable UUID versionId,
                                     JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        contentVersionService.restoreCourseVersion(courseId, versionId, userId);
    }
}
