package ru.offer.hunt.oh_course.model.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeTaskTestCaseDto {
    private UUID id;
    private String inputData;
    private String expectedOutput;
}
