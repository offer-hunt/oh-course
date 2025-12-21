package ru.offer.hunt.oh_course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.offer.hunt.oh_course.model.enums.PageType;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LessonPageOutlineLiteDto {
    UUID id;
    String title;
    PageType pageType;
    Integer sortOrder;
    List<QuestionOutlineLiteDto> questionOutlineLiteDtoList;
}
