package ru.offer.hunt.oh_course.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.QuestionDto;
import ru.offer.hunt.oh_course.model.dto.QuestionUpsertRequest;
import ru.offer.hunt.oh_course.service.QuestionService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/course/question")
public class QuestionController {
    private final QuestionService service;

    @GetMapping("/{id}")
    public QuestionDto get(@PathVariable("id") UUID id){
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") UUID id){
        service.delete(id);
    }

    @PostMapping("/{pageId}")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionDto create(@PathVariable("pageId") UUID pageID, @RequestBody QuestionUpsertRequest request){
        return service.create(pageID, request);
    }

    @PutMapping("/{id}")
    public QuestionDto update(@PathVariable("id") UUID id, @RequestBody QuestionUpsertRequest request){
        return service.update(id, request);
    }


}
