package ru.offer.hunt.oh_course.model.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.offer.hunt.oh_course.model.dto.QuestionDto;
import ru.offer.hunt.oh_course.model.dto.QuestionUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.Question;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    QuestionDto toDto(Question src);

    @Mapping(target = "pageId", source = "pageId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Question toEntity(UUID pageId, QuestionUpsertRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pageId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget Question target, QuestionUpsertRequest req);
}
