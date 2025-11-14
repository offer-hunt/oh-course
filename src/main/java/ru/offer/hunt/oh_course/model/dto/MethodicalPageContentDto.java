package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class MethodicalPageContentDto {
    private UUID pageId;
    private String markdown;
    private String externalVideoUrl;
    private OffsetDateTime updatedAt;
}
