package ru.offer.hunt.oh_course.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.QuestionDto;
import ru.offer.hunt.oh_course.model.dto.QuestionUpsertRequest;
import ru.offer.hunt.oh_course.model.mapper.QuestionMapper;
import ru.offer.hunt.oh_course.model.repository.LessonPageRepository;
import ru.offer.hunt.oh_course.model.repository.QuestionRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor
@Service
public class QuestionService {

    private final QuestionRepository rep;
    private final QuestionMapper map;
    private final LessonPageRepository lessonPageRepository;


    @Transactional(readOnly = true)
    public QuestionDto get(UUID id){
        var question = rep.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found")
        );

        return map.toDto(question);
    }

    @Transactional
    public void delete(UUID id){
        if(!rep.existsById(id)){
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        }

        rep.deleteById(id);
    }


    @Transactional
    public QuestionDto create(UUID pageId, QuestionUpsertRequest request) {
        if (!lessonPageRepository.existsById(pageId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson Page not found");
        }

        var question = map.toEntity(pageId, request);
        question.setCreatedAt(OffsetDateTime.now());
        question.setUpdatedAt(null);

        try {
            question = rep.save(question);
            return map.toDto(question);
        } catch (Exception ex) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось сохранить текст. Попробуйте позже"
            );

        }
    }

    @Transactional
    public QuestionDto update(UUID id, QuestionUpsertRequest request){
        var question = rep.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        map.update(question, request);
        question.setUpdatedAt(OffsetDateTime.now());

        try {
            question = rep.save(question);
            return map.toDto(question);
        } catch (Exception ex) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось сохранить текст. Попробуйте позже"
            );
        }

    }





}
