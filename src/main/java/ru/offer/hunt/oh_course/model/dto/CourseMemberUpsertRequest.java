package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.CourseMemberRole;

@Getter
@Setter
public class CourseMemberUpsertRequest {

    @NotNull
    private CourseMemberRole role;
}
