package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.AccessType;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;

import java.util.List;

@Getter
@Setter
public class CourseUpsertRequest {

    @NotBlank
    @Size(min = 10, max = 100, message = "Название должно быть от 10 до 100 символов")
    private String title;

    @NotBlank
    private String slug;

    @NotBlank
    @Size(max = 1000, message = "Описание должно быть не длиннее 1000 символов")
    private String description;

    @Size(max = 512)
    private String coverUrl;

    private String language;
    private String level;
    private Integer estimatedDurationMin;

    @NotNull
    private CourseStatus status;

    @NotNull
    private AccessType accessType;

    private String inviteCode;
    private Boolean requiresEntitlement;
    private Integer maxFreeEnrollments;
    private Integer version;

    @Size(max = 10, message = "Не более 10 тегов")
    private List<@Size(max = 15, message = "Тег не длиннее 15 символов") String> tags;
}
