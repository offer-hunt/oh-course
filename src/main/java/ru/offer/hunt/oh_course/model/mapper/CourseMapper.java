package ru.offer.hunt.oh_course.model.mapper;


import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.offer.hunt.oh_course.model.dto.CourseDto;
import ru.offer.hunt.oh_course.model.dto.CourseUpsertRequest;
import ru.offer.hunt.oh_course.model.dto.LessonDto;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.entity.TagRef;
import ru.offer.hunt.oh_course.model.repository.CourseMemberRepository;
import ru.offer.hunt.oh_course.model.repository.LessonRepository;
import ru.offer.hunt.oh_course.service.TagService;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "tags", source = "tagRefs")
    @Mapping(target = "estimatedDurationHours", source = "estimatedDurationMin")
    @Mapping(target = "membersCount", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    CourseDto toDto(Course src, @Context CourseMemberRepository memberRepository,
                    @Context LessonRepository lessonRepository, @Context LessonMapper lessonMapper);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "tagRefs", source = "tags")
    Course toEntity(CourseUpsertRequest req, @Context TagService tagService);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "tagRefs", source = "tags")
    void update(@MappingTarget Course target, CourseUpsertRequest req, @Context TagService tagService);

    default List<TagRef> mapTags(List<String> tagNames, @Context TagService tagService) {
        return tagService.resolveTags(tagNames);
    }

    @AfterMapping
    default List<Lesson> mapLessons(Course source,
                                    @MappingTarget CourseDto target,
                                    @Context LessonRepository lessonRepository,
                                    @Context LessonMapper lessonMapper) {
        List<Lesson> lessons = lessonRepository.findByCourseId(source.getId());
        List<LessonDto> lessonDtos = lessons.stream().map(lessonMapper::toDto).toList();
        target.setLessons(lessonDtos);
        return lessons;

    }

    @AfterMapping
    default void enrichCourseDto(
            Course source,
            @MappingTarget CourseDto target,
            @Context CourseMemberRepository memberRepository) {

        int membersCount = memberRepository.countByIdCourseId(source.getId());
        target.setMembersCount(membersCount);

        if (source.getEstimatedDurationMin() != null) {
            target.setEstimatedDurationHours(source.getEstimatedDurationMin() / 60);
        }
    }
}
