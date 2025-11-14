package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class TagRefDto {
    private UUID id;
    private String name;
    private OffsetDateTime createdAt;
}
