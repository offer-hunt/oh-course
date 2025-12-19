package ru.offer.hunt.oh_course.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.offer.hunt.oh_course.model.entity.*;

@Mapper(componentModel = "spring")
public interface CloningMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Lesson copyLesson(Lesson source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LessonPage copyPage(LessonPage source);

    @Mapping(target = "pageId", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MethodicalPageContent copyMethodical(MethodicalPageContent source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pageId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Question copyQuestion(Question source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questionId", ignore = true)
    QuestionOption copyOption(QuestionOption source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questionId", ignore = true)
    QuestionTestCase copyTestCase(QuestionTestCase source);
}