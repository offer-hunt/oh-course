package ru.offer.hunt.oh_course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.offer.hunt.oh_course.model.dto.CourseStructureDto;
import ru.offer.hunt.oh_course.service.CourseStructureService;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Course Structure", description = "Структура курса для интеграций (Learning/Analytics)")
public class CourseStructureController {

    private final CourseStructureService courseStructureService;

    @GetMapping("/{courseId}/structure")
    @Operation(summary = "Получить структуру курса (уроки/страницы/вопросы)")
    public CourseStructureDto getCourseStructure(@PathVariable UUID courseId) {
        return courseStructureService.getCourseStructure(courseId);
    }
}
