package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class QuestionTestCaseDto {
    private UUID id;
    private UUID questionId;
    private String inputData;
    private String expectedOutput;
    private Integer timeoutMs;
    private Integer memoryLimitMb;
}
