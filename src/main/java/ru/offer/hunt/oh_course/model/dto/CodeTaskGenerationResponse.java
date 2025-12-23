package ru.offer.hunt.oh_course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodeTaskGenerationResponse {
    private String description;
    private String exampleSolution;
    private List<CodeTestCaseDto> testCases;
}

