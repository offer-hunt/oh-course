package ru.offer.hunt.oh_course.service;

import lombok.AllArgsConstructor;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionOptionMapper questionOptionMapper;
    private final LessonPageRepository lessonPageRepository;

    @Transactional(readOnly = true)
    public QuestionDto get(UUID id){
        var question = questionRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found")
        );

        return questionMapper.toDto(question);
    }

    @Transactional
    public QuestionDto create(UUID pageId, QuestionUpsertRequest request, List<QuestionOptionUpsertRequest> optionals) {

        /// Проверяем правильность
        if (!lessonPageRepository.existsById(pageId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson Page not found");
        }
        boolean correctAnswer = false;
        for(QuestionOptionUpsertRequest req : optionals){
            if(req.getCorrect()){
                correctAnswer = true;
                break;
            }
        }

        if(!correctAnswer){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ни один из вариантов ответа не помечен как правильный");
        }
        ///

        var question = questionMapper.toEntity(pageId, request);
        question.setCreatedAt(OffsetDateTime.now());
        question.setUpdatedAt(null);

        try {
            question = questionRepository.saveAndFlush(question);

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось сохранить текст. Попробуйте позже"
            );
        }

        UUID questionId = question.getId();

        for(QuestionOptionUpsertRequest req : optionals){
            var optional = questionOptionMapper.toEntity(questionId, req);

            try{
                optional = questionOptionRepository.save(optional);
            } catch (Exception ex) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Не удалось сохранить текст. Попробуйте позже"
                );
            }

        }
        return questionMapper.toDto(question) ;
    }

    @Transactional(readOnly = true)
    public List<QuestionOptionDto> getAllOptionsByQuestionId(UUID questionId){
        if(! questionRepository.existsById(questionId)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        }

        return questionOptionRepository.findByQuestionIdOrderBySortOrderAsc(questionId).stream().map(questionOptionMapper::toDto).collect(Collectors.toList());
    }

}
