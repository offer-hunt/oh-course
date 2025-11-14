package ru.offer.hunt.oh_course.model.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.offer.hunt.oh_course.model.dto.CourseMemberDto;
import ru.offer.hunt.oh_course.model.dto.CourseMemberUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.CourseMember;
import ru.offer.hunt.oh_course.model.id.CourseMemberId;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        imports = {CourseMemberId.class})
public interface CourseMemberMapper {

    @Mapping(source = "id.courseId", target = "courseId")
    @Mapping(source = "id.userId", target = "userId")
    CourseMemberDto toDto(CourseMember src);

    @Mapping(target = "id", expression = "java(new CourseMemberId(courseId, userId))")
    @Mapping(target = "role", source = "req.role")
    @Mapping(target = "addedAt", ignore = true)
    @Mapping(target = "addedBy", ignore = true)
    CourseMember toEntity(UUID courseId, UUID userId, CourseMemberUpsertRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addedAt", ignore = true)
    @Mapping(target = "addedBy", ignore = true)
    void update(@MappingTarget CourseMember target, CourseMemberUpsertRequest req);
}
