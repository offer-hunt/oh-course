package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentVersionDetailsDto extends ContentVersionSummaryDto {

    private String payloadJson;
}
