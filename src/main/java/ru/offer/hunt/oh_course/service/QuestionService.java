package ru.offer.hunt.oh_course.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.*;
import ru.offer.hunt.oh_course.model.mapper.QuestionMapper;
import ru.offer.hunt.oh_course.model.mapper.QuestionOptionMapper;
import ru.offer.hunt.oh_course.model.repository.LessonPageRepository;
import ru.offer.hunt.oh_course.model.repository.QuestionOptionRepository;
import ru.offer.hunt.oh_course.model.repository.QuestionRepository;

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

    private final static Integer MAX_SIZE = 1000000;

    @Transactional(readOnly = true)
    public QuestionDto get(UUID id) {
        var question = questionRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found")
        );

        return questionMapper.toDto(question);
    }

    @Transactional
    public QuestionDto create(UUID pageId, QuestionUpsertRequest request, List<QuestionOptionUpsertRequest> optionals) {

        // Проверяем существование страницы урока
        if (!lessonPageRepository.existsById(pageId)) {
            log.error("Question create failed - lesson page not found, pageId={}", pageId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson Page not found");
        }

        // Проверяем наличие хотя бы одного правильного ответа
        boolean correctAnswer = optionals.stream().anyMatch(QuestionOptionUpsertRequest::getCorrect);
        if (!correctAnswer) {
            log.error("Question create failed - no correct answer, pageId={}", pageId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ни один из вариантов ответа не помечен как правильный");
        }

        var question = questionMapper.toEntity(pageId, request);
        question.setCreatedAt(OffsetDateTime.now());
        question.setUpdatedAt(null);

        try {
            question = questionRepository.saveAndFlush(question);
        } catch (Exception ex) {
            log.error("Question  create failed - server error, pageId={}", pageId, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось сохранить тест. Попробуйте позже"
            );
        }

        UUID questionId = question.getId();

        for (QuestionOptionUpsertRequest req : optionals) {
            var optional = questionOptionMapper.toEntity(questionId, req);

            try {
                optional = questionOptionRepository.save(optional);
            } catch (Exception ex) {
                log.error("Question content save failed - server error",  ex);
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Не удалось сохранить тест. Попробуйте позже"
                );
            }
        }

        log.info("Test page saved: pageId={}, questionId={}", pageId, questionId);

        return questionMapper.toDto(question);
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
    public QuestionDto  createDetailedAnswer(UUID pageId, QuestionUpsertRequest request){
        if (!lessonPageRepository.existsById(pageId)) {
            log.error("Detailed Question create failed - lesson page not found, pageId={}", pageId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson Page not found");
        }

        if (request.getCorrectAnswer().isBlank()) {
            log.error("Detailed Question create failed - no correct answer, pageId={}", pageId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нет варианта ответа");
        }

        if(request.getType() != TEXT_INPUT){
            log.error("Detailed Question create failed - wrong type, pageId={}", pageId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Поле для ответа не определено");
        }

        if(request.getText().length() > MAX_SIZE || request.getCorrectAnswer().length() > MAX_SIZE){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Слишком длинный текст");
        }
        if(request.getUseAiCheck()){
            if(!checkAI(request)){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный промт. Исправьте формулировку");
            }
        }

        var question = questionMapper.toEntity(pageId, request);
        question.setCreatedAt(OffsetDateTime.now());
        question.setUpdatedAt(null);


        try {
            question = questionRepository.saveAndFlush(question);
        } catch (Exception ex) {
            log.error("Detailed question  save failed - server error, pageId={}", pageId, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось сохранить тест. Попробуйте позже"
            );
        }

        log.info("Detailed answer page saved: pageId={}, questionId={}", pageId, question.getId());

        return questionMapper.toDto(question);

    }

    private boolean checkAI(QuestionUpsertRequest request){
        /// Тут заглушка Проверка AI
        return true;
    }
}
