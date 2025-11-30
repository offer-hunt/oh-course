package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QuestionAnswerDto {
    private QuestionDto question;
    private List<QuestionOptionDto> options;

    public QuestionAnswerDto() {
        options = new ArrayList<>();
        question = null;
    }

    public void add(QuestionOptionDto dto){
        options.add(dto);
    }
}
