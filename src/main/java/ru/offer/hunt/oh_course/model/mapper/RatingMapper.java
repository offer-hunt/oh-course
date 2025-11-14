package ru.offer.hunt.oh_course.model.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.offer.hunt.oh_course.model.dto.RatingDto;
import ru.offer.hunt.oh_course.model.dto.RatingUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.Rating;
import ru.offer.hunt.oh_course.model.id.RatingId;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        imports = {RatingId.class})
public interface RatingMapper {

    @Mapping(source = "id.userId", target = "userId")
    @Mapping(source = "id.courseId", target = "courseId")
    RatingDto toDto(Rating src);

    @Mapping(target = "id", expression = "java(new RatingId(userId, courseId))")
    @Mapping(target = "value", source = "req.value")
    @Mapping(target = "comment", source = "req.comment")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Rating toEntity(UUID userId, UUID courseId, RatingUpsertRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget Rating target, RatingUpsertRequest req);
}
