package ru.offer.hunt.oh_course.model.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import ru.offer.hunt.oh_course.model.enums.PageType;

@Value
@Builder
public class PageViewDto {
    UUID pageId;
    String title;
    PageType pageType;
    boolean readOnly;

    MethodicalContentDto methodical; // только для THEORY
    List<QuestionViewDto> questions; // для TEST/CODE_TASK

    CtaDto cta;
}
