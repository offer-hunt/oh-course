package ru.offer.hunt.oh_course.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentDto;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentUpsertRequest;
import ru.offer.hunt.oh_course.service.MethodicalPageContentService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/course/methodical-page-content")
public class MethodicalPageContentController {
    private final MethodicalPageContentService methodicalPageContentService;


    @Transactional
    @PostMapping("/create/{pageId}")
    MethodicalPageContentDto create(@PathVariable ("pageId") UUID pageId, @RequestBody @Valid MethodicalPageContentUpsertRequest methodicalPageContentUpsertRequest){
        return methodicalPageContentService.create(pageId, methodicalPageContentUpsertRequest);
    }
}
