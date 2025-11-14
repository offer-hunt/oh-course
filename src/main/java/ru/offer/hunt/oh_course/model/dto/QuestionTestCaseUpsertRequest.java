package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionTestCaseUpsertRequest {

    @NotBlank
    private String inputData;

    @NotBlank
    private String expectedOutput;

    private Integer timeoutMs;
    private Integer memoryLimitMb;
}
