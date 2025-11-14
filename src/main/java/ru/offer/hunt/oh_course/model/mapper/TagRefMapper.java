package ru.offer.hunt.oh_course.model.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.offer.hunt.oh_course.model.dto.TagRefDto;
import ru.offer.hunt.oh_course.model.dto.TagRefUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.TagRef;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TagRefMapper {

    TagRefDto toDto(TagRef src);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    TagRef toEntity(UUID id, TagRefUpsertRequest req);
}
