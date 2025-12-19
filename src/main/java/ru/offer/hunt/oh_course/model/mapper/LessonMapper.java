package ru.offer.hunt.oh_course.model.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.offer.hunt.oh_course.model.dto.LessonDto;
import ru.offer.hunt.oh_course.model.dto.LessonUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.Lesson;


@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(target = "courseId", source = "course.id")
    LessonDto toDto(Lesson src);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "demo", expression = "java(Boolean.TRUE.equals(req.getDemo()))")
    Lesson toEntity(LessonUpsertRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "demo", expression = "java(req.getDemo() == null ? target.isDemo() : req.getDemo())")
    void update(@MappingTarget Lesson target, LessonUpsertRequest req);
}
