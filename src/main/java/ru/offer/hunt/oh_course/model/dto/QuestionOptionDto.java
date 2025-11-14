package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class QuestionOptionDto {
    private UUID id;
    private UUID questionId;
    private String label;
    private boolean correct;
    private Integer sortOrder;
}