package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeTaskTestCaseRequest {

    private String inputData;
    private String expectedOutput;
}
