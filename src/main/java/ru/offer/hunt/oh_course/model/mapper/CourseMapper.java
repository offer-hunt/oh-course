package ru.offer.hunt.oh_course.model.mapper;


import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.offer.hunt.oh_course.model.dto.CourseDto;
import ru.offer.hunt.oh_course.model.dto.CourseUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.Course;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    CourseDto toDto(Course src);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Course toEntity(CourseUpsertRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget Course target, CourseUpsertRequest req);
}
