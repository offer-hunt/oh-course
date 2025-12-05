package ru.offer.hunt.oh_course.model.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter

public class LessonWithPagesDto {
    private LessonDto lessonDto;
    private List<LessonPageDto> lessonPageDto;

    public LessonWithPagesDto(LessonDto lessonDto){
        lessonPageDto = new ArrayList<>();
        this.lessonDto = lessonDto;
    }

    public LessonWithPagesDto(LessonDto lessonDto, List<LessonPageDto> lessonPageDto){
        this.lessonDto = lessonDto;
        this.lessonPageDto = (lessonPageDto == null) ? new ArrayList<>() : lessonPageDto;
    }
}
