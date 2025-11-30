package ru.offer.hunt.oh_course.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.*;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.entity.LessonPage;
import ru.offer.hunt.oh_course.model.enums.CourseMemberRole;
import ru.offer.hunt.oh_course.model.mapper.QuestionMapper;
import ru.offer.hunt.oh_course.model.mapper.QuestionOptionMapper;
import ru.offer.hunt.oh_course.model.repository.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.offer.hunt.oh_course.model.enums.QuestionType.TEXT_INPUT;

@AllArgsConstructor
@Service
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionOptionMapper questionOptionMapper;
    private final LessonPageRepository lessonPageRepository;

    private final LessonRepository lessonRepository;
    private final CourseMemberRepository courseMemberRepository;

    private final static Integer MAX_SIZE = 1000000;

    @Transactional(readOnly = true)
    public QuestionDto get(UUID id) {
        var question = questionRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found")
        );

        return questionMapper.toDto(question);
    }

    @Transactional
    public QuestionDto create(UUID pageId, QuestionUpsertRequest request, List<QuestionOptionUpsertRequest> optionals, UUID userId) {
        try {
            if (optionals == null || optionals.isEmpty()) {
                log.error("Question create failed - no answer, pageId={}", pageId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нет ни одного варианта ответа");
            }

            /// Проверяем все ответы, так как поле correct - может быть null
            for (QuestionOptionUpsertRequest req : optionals) {
                if(req.getCorrect() == null){
                    req.setCorrect(false);
                }
            }

            /// Проверяем наличие хотя бы одного правильного ответа
            boolean correctAnswer = optionals.stream().anyMatch(QuestionOptionUpsertRequest::getCorrect);
            if (!correctAnswer) {
                log.error("Question create failed - no correct answer, pageId={}", pageId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ни один из вариантов ответа не помечен как правильный");
            }

            checkRightUserAndExistenceCourse(pageId, userId);

            if (request.getUseAiCheck() == null) {
                request.setUseAiCheck(false);
            }
            var question = questionMapper.toEntity(pageId, request);
            question.setId(UUID.randomUUID());
            question.setCreatedAt(OffsetDateTime.now());
            question.setUpdatedAt(null);

            question = questionRepository.saveAndFlush(question);

            UUID questionId = question.getId();


            for (QuestionOptionUpsertRequest req : optionals) {
                var optional = questionOptionMapper.toEntity(questionId, req);
                    optional = questionOptionRepository.save(optional);
            }

            log.info("Test page saved: pageId={}, questionId={}", pageId, questionId);

            return questionMapper.toDto(question);
        }catch (ResponseStatusException e) {
            throw e;
        }catch (Exception ex) {
            log.error("Question  create failed - server error, pageId={}", pageId, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось сохранить тест. Попробуйте позже"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<QuestionOptionDto> getAllOptionsByQuestionId(UUID questionId) {
        if (!questionRepository.existsById(questionId)) {
            log.error("Get options failed - question not found, questionId={}", questionId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        }

        return questionOptionRepository.findByQuestionIdOrderBySortOrderAsc(questionId)
                .stream()
                .map(questionOptionMapper::toDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public QuestionDto  createDetailedAnswer(UUID pageId, QuestionUpsertRequest request, UUID userId){

        try {
            /// Первоначальная проверка DTO
            /// Проверка наличия ответа, наличие вопроса проверяется автоматически, аннотацией @Valid
            if (request.getCorrectAnswer().isBlank()) {
                log.error("Detailed Question create failed - no correct answer, pageId={}", pageId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нет варианта ответа");
            }

            /// Проверка типа вопроса
            if(request.getType() != TEXT_INPUT){
                log.error("Detailed Question create failed - wrong type, pageId={}", pageId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Поле для ответа не определено");
            }

            /// Проверка длины вопроса
            if(request.getText().length() > MAX_SIZE){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Слишком длинный вопрос");
            }

            /// Проверка длины ответа
            if(request.getCorrectAnswer().length() > MAX_SIZE){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Слишком длинный ответ");
            }

            checkRightUserAndExistenceCourse(pageId, userId);

            /// Проверка поля use_ai_check
            if(request.getUseAiCheck() == null){
                request.setUseAiCheck(false);
            }

            if(request.getUseAiCheck()){
                if(!checkAI(request)){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный промт. Исправьте формулировку");
                }
            }


            var question = questionMapper.toEntity(pageId, request);
            question.setId(UUID.randomUUID());
            question.setCreatedAt(OffsetDateTime.now());
            question.setUpdatedAt(null);

            question = questionRepository.saveAndFlush(question);
            return questionMapper.toDto(question);

        }catch (ResponseStatusException e) {
            throw e;
        }
        catch (Exception ex) {
            log.error("Detailed question  save failed - server error, pageId={}", pageId, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось сохранить тест. Попробуйте позже"
            );
        }
    }

    private boolean checkAI(QuestionUpsertRequest request){
        /// Тут заглушка Проверка AI
        return true;
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

    private void checkRightUserAndExistenceCourse(UUID pageId, UUID userId){
        /// Проверяем существование страницы, урока, курса + достаем Id курса, для проверки прав (это можно будет потом как-то оптимизировать, чтобы не делать 3 GET запроса, но это потом)
        LessonPage page = lessonPageRepository.findById(pageId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена"));

        UUID lessonId = page.getLessonId();
        if (lessonId == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось определить урок для страницы на которую мы загружаем вопрос с текстовым ответом"
            );
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Не удалось определить урок для страницы на которую мы загружаем вопрос с текстовым ответом"
                        ));

        UUID courseId = lesson.getCourseId();
        if (courseId == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось определить курс для страницы на которую мы загружаем вопрос с текстовым ответом"
            );
        }

        /// Проверка прав
        ensureCanEditCodeTasks(courseId, userId);
    }


}
