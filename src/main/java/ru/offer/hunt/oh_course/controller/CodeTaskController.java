package ru.offer.hunt.oh_course.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.offer.hunt.oh_course.model.dto.CodeTaskDto;
import ru.offer.hunt.oh_course.model.dto.CodeTaskUpsertRequest;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.CodeTaskService;

import java.util.UUID;

@RestController
@RequestMapping("/api/pages/{pageId}/code-task")
@RequiredArgsConstructor
@Slf4j
public class CodeTaskController {

    private final CodeTaskService codeTaskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CodeTaskDto addOrUpdateCodeTask(
            @PathVariable UUID pageId,
            @Valid @RequestBody CodeTaskUpsertRequest request,
            JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        return codeTaskService.saveCodeTask(pageId, userId, request);
    }

}
