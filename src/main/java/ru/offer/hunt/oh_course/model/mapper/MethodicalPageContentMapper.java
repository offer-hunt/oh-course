package ru.offer.hunt.oh_course.model.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentDto;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.MethodicalPageContent;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MethodicalPageContentMapper {

    MethodicalPageContentDto toDto(MethodicalPageContent src);

    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "pageId", source = "pageId")
    MethodicalPageContent toEntity(UUID pageId, MethodicalPageContentUpsertRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "pageId", ignore = true)
    void update(@MappingTarget MethodicalPageContent target,
                MethodicalPageContentUpsertRequest req);
}
