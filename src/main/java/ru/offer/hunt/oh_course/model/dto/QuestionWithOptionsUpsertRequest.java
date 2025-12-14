package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.Valid;

import java.util.List;


public record QuestionWithOptionsUpsertRequest(
        @Valid QuestionUpsertRequest question,
        @Valid List<QuestionOptionUpsertRequest> options
) {}