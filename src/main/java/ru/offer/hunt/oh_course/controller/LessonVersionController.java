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
@RequestMapping("/api/lessons/{lessonId}/versions")
@RequiredArgsConstructor
@Slf4j
public class LessonVersionController {

    private final ContentVersionService contentVersionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContentVersionDetailsDto saveLessonVersion(@PathVariable UUID lessonId,
                                                      @Valid @RequestBody(required = false)
                                                      VersionSaveRequest request,
                                                      JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        return contentVersionService.saveLessonVersion(lessonId, userId, request);
    }

    @GetMapping
    public List<ContentVersionSummaryDto> listLessonVersions(@PathVariable UUID lessonId,
                                                             JwtAuthenticationToken authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        return contentVersionService.getLessonVersions(lessonId, userId);
    }

    @GetMapping("/{versionId}")
    public ContentVersionDetailsDto getLessonVersion(@PathVariable UUID lessonId,
                                                     @PathVariable UUID versionId,
                                                     JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        return contentVersionService.getLessonVersion(lessonId, versionId, userId);
    }

    @PostMapping("/{versionId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreLessonVersion(@PathVariable UUID lessonId,
                                     @PathVariable UUID versionId,
                                     JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        contentVersionService.restoreLessonVersion(lessonId, versionId, userId);
    }
}
