package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.PageType;

@Getter
@Setter
public class LessonPageUpsertRequest {

    @NotBlank
    private String title;

    @NotNull
    private PageType pageType;

    @NotNull
    private Integer sortOrder;
}
