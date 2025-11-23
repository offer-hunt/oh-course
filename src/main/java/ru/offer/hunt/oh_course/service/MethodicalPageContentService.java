package ru.offer.hunt.oh_course.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentDto;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.entity.LessonPage;
import ru.offer.hunt.oh_course.model.enums.CourseMemberRole;
import ru.offer.hunt.oh_course.model.enums.PageType;
import ru.offer.hunt.oh_course.model.mapper.MethodicalPageContentMapper;
import ru.offer.hunt.oh_course.model.repository.CourseMemberRepository;
import ru.offer.hunt.oh_course.model.repository.LessonPageRepository;
import ru.offer.hunt.oh_course.model.repository.LessonRepository;
import ru.offer.hunt.oh_course.model.repository.MethodicalPageContentRepository;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MethodicalPageContentService {

    private final MethodicalPageContentMapper methodicalPageContentMapper;
    private final MethodicalPageContentRepository methodicalPageContentRepository;
    private final LessonPageRepository lessonPageRepository;
    private final static Integer MAX_SIZE = 100000;

    private final CourseMemberRepository courseMemberRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public MethodicalPageContentDto create(UUID pageId, MethodicalPageContentUpsertRequest methodicalPageContentUpsertRequest,  UUID userId) {

        /// Проверка длины, но лучше заменить на проверку по размеру файла, так мы будем учитывать кодировку
        if (methodicalPageContentUpsertRequest.getMarkdown().length() > MAX_SIZE) {
            log.error("Markdown length is too long, length = {}", methodicalPageContentUpsertRequest.getMarkdown().length());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Размер markdown файла слишком большой");
        }

        /// Проверяем существование страницы, урока, курса + достаем Id курса, для проверки прав (это можно будет потом как-то оптимизировать, чтобы не делать 3 GET запроса, но это потом)
        try {
            LessonPage page = lessonPageRepository.findById(pageId)
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена"));

            UUID lessonId = page.getLessonId();
            if (lessonId == null) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Не удалось определить урок для страницы на которую мы загружаем markdown файл"
                );
            }

            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "Не удалось определить урок для страницы на которую мы загружаем markdown файл"
                            ));

            UUID courseId = lesson.getCourseId();
            if (courseId == null) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Не удалось определить курс для страницы на которую мы загружаем markdown файл"
                );
            }

            /// Проверка прав
            ensureCanEditCodeTasks(courseId, userId);

            /// Создаем объект из DTO
            var methodicalPage = methodicalPageContentMapper.toEntity(pageId, methodicalPageContentUpsertRequest);
            methodicalPage.setPageId(UUID.randomUUID());
            methodicalPage.setUpdatedAt(null);

            /// Сохраняем и возвращаем
            methodicalPage = methodicalPageContentRepository.save(methodicalPage);
            return methodicalPageContentMapper.toDto(methodicalPage);



        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {

            log.error("Не сохранили объект - мета данные на странице с id = {}", pageId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не получилось сохранить мета данные, попробуйте позже");

        }
    }

    private void ensureCanEditCodeTasks(UUID courseId, UUID userId) {
        boolean allowed = courseMemberRepository
                .existsByIdCourseIdAndIdUserIdAndRoleIn(
                        courseId,
                        userId,
                        List.of(CourseMemberRole.OWNER, CourseMemberRole.ADMIN)
                );

        if (!allowed) {
            log.warn(
                    "Code page edit forbidden: courseId={}, userId={}",
                    courseId,
                    userId
            );
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Недостаточно прав для редактирования кодового задания"
            );
        }
    }
}
