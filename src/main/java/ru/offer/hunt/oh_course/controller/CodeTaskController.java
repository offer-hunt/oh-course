package ru.offer.hunt.oh_course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.CodeTaskDto;
import ru.offer.hunt.oh_course.model.dto.CodeTaskUpsertRequest;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.CodeTaskService;

import java.util.UUID;

@RestController
@RequestMapping("/api/pages/{pageId}/code-task")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Code tasks", description = "Управление кодовыми заданиями на страницах курса")
public class CodeTaskController {

    private final CodeTaskService codeTaskService;

    @Operation(
            summary = "Создать или обновить кодовое задание на странице",
            description = """
                    Привязывает к CODE_TASK-странице текст задания и набор тест-кейсов. \
                    Если кодовое задание уже существует, оно перезаписывается, а старые тесты удаляются."""
    )
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
