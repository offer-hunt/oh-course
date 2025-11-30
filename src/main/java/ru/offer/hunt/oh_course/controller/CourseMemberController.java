package ru.offer.hunt.oh_course.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.CourseMemberDto;
import ru.offer.hunt.oh_course.model.dto.CourseMemberUpsertRequest;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.CourseMemberService;

@RestController
@RequestMapping("/api/courses/{courseId}/members")
@RequiredArgsConstructor
@Slf4j
public class CourseMemberController {

    private final CourseMemberService courseMemberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseMemberDto addCollaborator(@PathVariable UUID courseId,
                                           @Valid @RequestBody CourseMemberUpsertRequest request,
                                           JwtAuthenticationToken authentication) {

        UUID currentUserId = SecurityUtils.getUserId(authentication);
        return courseMemberService.addCollaborator(courseId, currentUserId, request);
    }
}
