package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MethodicalPageContentUpsertRequest {

    @NotBlank
    private String markdown;

    private String externalVideoUrl;
}
