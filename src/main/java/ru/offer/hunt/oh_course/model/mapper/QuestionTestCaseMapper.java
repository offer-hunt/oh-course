package ru.offer.hunt.oh_course.model.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.offer.hunt.oh_course.model.dto.QuestionTestCaseDto;
import ru.offer.hunt.oh_course.model.dto.QuestionTestCaseUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.QuestionTestCase;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface QuestionTestCaseMapper {

    QuestionTestCaseDto toDto(QuestionTestCase src);

    @Mapping(target = "questionId", source = "questionId")
    @Mapping(target = "id", ignore = true)
    QuestionTestCase toEntity(UUID questionId, QuestionTestCaseUpsertRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questionId", ignore = true)
    void update(@MappingTarget QuestionTestCase target, QuestionTestCaseUpsertRequest req);
}
