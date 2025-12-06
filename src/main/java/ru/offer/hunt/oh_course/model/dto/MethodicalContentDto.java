package ru.offer.hunt.oh_course.model.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MethodicalContentDto {
    String markdown;
    String externalVideoUrl;
}
