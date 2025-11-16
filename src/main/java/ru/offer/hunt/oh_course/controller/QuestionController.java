package ru.offer.hunt.oh_course.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.*;
import ru.offer.hunt.oh_course.service.QuestionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/course/question")
public class QuestionController {
    private final QuestionService service;

    @GetMapping("getById/{id}")
    public QuestionDto get(@PathVariable("id") UUID id){
        return service.get(id);
    }

    @GetMapping("getAllOptionsByQuestionID/{questionId}")
    public List<QuestionOptionDto> getAllOptions(@PathVariable("questionId") UUID id){
        return service.getAllOptionsByQuestionId(id);
    }

    @PostMapping("createWithChoice/{pageId}")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionDto create(@PathVariable("pageId") UUID pageID, @RequestBody @Valid QuestionUpsertRequest request, @RequestBody @Valid List<QuestionOptionUpsertRequest> optionals){
        return service.create(pageID, request, optionals);
    }

    @PostMapping("createDetailedAnswer/{questionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionDto createDetailedAnswer(@PathVariable("pageId") UUID pageID, @RequestBody @Valid QuestionUpsertRequest request, @RequestBody @Valid QuestionOptionUpsertRequest optional){
        return service.createDetailedAnswer(pageID, request);
    }


}
