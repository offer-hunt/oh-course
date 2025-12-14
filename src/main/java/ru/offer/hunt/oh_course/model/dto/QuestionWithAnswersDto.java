package ru.offer.hunt.oh_course.model.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QuestionWithAnswersDto {
    QuestionDto questionDto;
    List<QuestionOptionDto> questionOptionDtoList;
    List<QuestionTestCaseDto> questionTestCaseDtoList;

    public QuestionWithAnswersDto(QuestionDto questionDto, List<QuestionOptionDto> questionOptionDtoList, List<QuestionTestCaseDto> questionTestCaseDtoList){
        this.questionDto = questionDto;
        this.questionOptionDtoList = (questionOptionDtoList == null) ? new ArrayList<>() : questionOptionDtoList;
        this.questionTestCaseDtoList = (questionTestCaseDtoList == null) ? new ArrayList<>() : questionTestCaseDtoList;
    }
}
