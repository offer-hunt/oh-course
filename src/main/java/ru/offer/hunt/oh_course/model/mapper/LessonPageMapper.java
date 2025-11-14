package ru.offer.hunt.oh_course.model.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.offer.hunt.oh_course.model.dto.LessonPageDto;
import ru.offer.hunt.oh_course.model.dto.LessonPageUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.LessonPage;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface LessonPageMapper {

    LessonPageDto toDto(LessonPage src);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessonId", source = "lessonId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LessonPage toEntity(UUID lessonId, LessonPageUpsertRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessonId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget LessonPage target, LessonPageUpsertRequest req);
}
