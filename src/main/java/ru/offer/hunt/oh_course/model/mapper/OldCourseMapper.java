package ru.offer.hunt.oh_course.model.mapper;

import org.mapstruct.Mapper;
import ru.offer.hunt.oh_course.model.dto.OldCourseDto;
import ru.offer.hunt.oh_course.model.entity.Course;

@Mapper(componentModel = "spring")
public interface OldCourseMapper {
    OldCourseDto toDto(Course src);
}
