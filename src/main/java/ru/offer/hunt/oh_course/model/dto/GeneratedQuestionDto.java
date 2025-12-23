package ru.offer.hunt.oh_course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedQuestionDto {
    private String text;
    private List<String> options;
    private List<Integer> correctIndices; // индексы правильных ответов
}

