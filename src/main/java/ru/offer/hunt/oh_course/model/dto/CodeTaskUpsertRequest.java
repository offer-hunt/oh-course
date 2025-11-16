package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.Valid;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.CodeLanguage;

@Getter
@Setter
public class CodeTaskUpsertRequest {

    private String description;

    private CodeLanguage language;

    @Valid
    private List<CodeTaskTestCaseRequest> testCases;
}
