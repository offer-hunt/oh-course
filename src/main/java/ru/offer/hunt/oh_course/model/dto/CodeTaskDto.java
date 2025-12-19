package ru.offer.hunt.oh_course.model.dto;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.CodeLanguage;

@Getter
@Setter
public class CodeTaskDto {

    private UUID questionId;
    private UUID pageId;

    private String description;
    private CodeLanguage language;

    private List<CodeTaskTestCaseDto> testCases;
}
