package ru.offer.hunt.oh_course.model.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PageDataDto {
    MethodicalPageContentDto methodicalPageContentDto;
    List<QuestionWithAnswersDto> questions;

}
