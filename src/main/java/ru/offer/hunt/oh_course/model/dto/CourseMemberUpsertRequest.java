package ru.offer.hunt.oh_course.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.CourseMemberRole;

@Getter
@Setter
public class CourseMemberUpsertRequest {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email")
    private String email;

    @NotNull(message = "Роль обязательна")
    private CourseMemberRole role;
}
