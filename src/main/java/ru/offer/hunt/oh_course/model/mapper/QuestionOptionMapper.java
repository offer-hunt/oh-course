package ru.offer.hunt.oh_course.model.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.offer.hunt.oh_course.model.dto.QuestionOptionDto;
import ru.offer.hunt.oh_course.model.dto.QuestionOptionUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.QuestionOption;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface QuestionOptionMapper {

    QuestionOptionDto toDto(QuestionOption src);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questionId", source = "questionId")
    @Mapping(target = "correct", expression = "java(Boolean.TRUE.equals(req.getCorrect()))")
    QuestionOption toEntity(UUID questionId, QuestionOptionUpsertRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questionId", ignore = true)
    @Mapping(target = "correct", expression = "java(req.getCorrect() == null ? target.isCorrect() : req.getCorrect())")
    void update(@MappingTarget QuestionOption target, QuestionOptionUpsertRequest req);
}
