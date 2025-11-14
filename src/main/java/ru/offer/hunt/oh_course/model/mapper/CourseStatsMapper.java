package ru.offer.hunt.oh_course.model.mapper;

import org.mapstruct.Mapper;
import ru.offer.hunt.oh_course.model.dto.CourseStatsDto;
import ru.offer.hunt.oh_course.model.entity.CourseStats;

@Mapper(componentModel = "spring")
public interface CourseStatsMapper {

    CourseStatsDto toDto(CourseStats src);
}
