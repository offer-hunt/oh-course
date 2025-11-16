package ru.offer.hunt.oh_course.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentDto;
import ru.offer.hunt.oh_course.service.MethodicalPageContentService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/course/methodical-page")
public class MethodicalPageContentController {

    private final MethodicalPageContentService methodicalPageContentService;

    @GetMapping("/{pageID}")
    MethodicalPageContentDto get(@PathVariable ("pageID")UUID id){
        return null;
    }
}
