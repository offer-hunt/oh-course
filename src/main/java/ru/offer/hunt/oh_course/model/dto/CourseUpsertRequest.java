package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.AccessType;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;

@Getter
@Setter
public class CourseUpsertRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String slug;

    private String description;
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
}
