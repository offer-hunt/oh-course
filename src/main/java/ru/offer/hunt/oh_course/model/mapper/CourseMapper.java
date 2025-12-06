package ru.offer.hunt.oh_course.model.mapper;

import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.offer.hunt.oh_course.model.dto.CourseDto;
import ru.offer.hunt.oh_course.model.dto.CourseUpsertRequest;
import ru.offer.hunt.oh_course.model.dto.LessonDto;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.entity.CourseStats;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.entity.TagRef;
import ru.offer.hunt.oh_course.model.repository.CourseStatsRepository;
import ru.offer.hunt.oh_course.model.repository.LessonRepository;
import ru.offer.hunt.oh_course.service.TagService;

@Mapper(componentModel = "spring", uses = {TagRefMapper.class})
public interface CourseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "requiresEntitlement", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)

    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "lessons", ignore = true)

    @Mapping(target = "tagRefs", source = "tags", qualifiedByName = "mapTags")
    Course toEntity(CourseUpsertRequest src, @Context TagService tagService);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "requiresEntitlement", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)

    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "lessons", ignore = true)

    @Mapping(target = "tagRefs", source = "tags", qualifiedByName = "mapTags")
    void updateEntity(@MappingTarget Course target, CourseUpsertRequest src, @Context TagService tagService);

    @Named("mapTags")
    default List<TagRef> mapTags(List<String> tags, @Context TagService tagService) {
        return tagService.resolveTagRefs(tags);
    }

    @Mapping(target = "tags", source = "tagRefs")
    @Mapping(target = "estimatedDurationHours", ignore = true)
    @Mapping(target = "membersCount", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    CourseDto toDto(
            Course src,
            @Context LessonRepository lessonRepository,
            @Context LessonMapper lessonMapper,
            @Context CourseStatsRepository courseStatsRepository
    );

    @AfterMapping
    default void enrich(
            Course source,
            @MappingTarget CourseDto target,
            @Context LessonRepository lessonRepository,
            @Context LessonMapper lessonMapper,
            @Context CourseStatsRepository courseStatsRepository
    ) {
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(source.getId());
        List<LessonDto> lessonDtos = lessons.stream().map(lessonMapper::toDto).toList();
        target.setLessons(lessonDtos);

        int enrollments = courseStatsRepository.findById(source.getId())
                .map(CourseStats::getEnrollments)
                .orElse(0);
        target.setMembersCount(enrollments);

        Integer min = source.getEstimatedDurationMin();
        if (min != null && min > 0) {
            int hours = (min + 59) / 60;
            target.setEstimatedDurationHours(hours);
        } else {
            target.setEstimatedDurationHours(null);
        }
    }
}
