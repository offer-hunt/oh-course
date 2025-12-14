package ru.offer.hunt.oh_course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Course members", description = "Управление участниками и соавторами курса")
public class CourseMemberController {

    private final CourseMemberService courseMemberService;

    @Operation(
            summary = "Добавить соавтора к курсу",
            description = """
                    Добавляет пользователя в участники курса с указанной ролью (OWNER/ADMIN). \
                    Поиск пользователя идёт по email через UserDirectoryClient. \
                    Требует, чтобы текущий пользователь имел роль OWNER/ADMIN в этом курсе."""
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseMemberDto addCollaborator(@PathVariable UUID courseId,
                                           @Valid @RequestBody CourseMemberUpsertRequest request,
                                           JwtAuthenticationToken authentication) {

        UUID currentUserId = SecurityUtils.getUserId(authentication);
        return courseMemberService.addCollaborator(courseId, currentUserId, request);
    }
}
