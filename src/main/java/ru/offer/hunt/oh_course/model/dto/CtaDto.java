package ru.offer.hunt.oh_course.model.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CtaDto {
    String type; // "ENROLL"
    String text;
}
