package ru.offer.hunt.oh_course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.PageDataDto;
import ru.offer.hunt.oh_course.model.dto.QuestionOptionDto;
import ru.offer.hunt.oh_course.model.dto.QuestionTestCaseDto;
import ru.offer.hunt.oh_course.model.dto.QuestionWithAnswersDto;
import ru.offer.hunt.oh_course.model.entity.*;
import ru.offer.hunt.oh_course.model.mapper.*;
import ru.offer.hunt.oh_course.model.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageDataGetService {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonPageRepository lessonPageRepository;

    private final MethodicalPageContentRepository methodicalPageContentRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionTestCaseRepository questionTestCaseRepository;

    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final QuestionTestCaseMapper questionTestCaseMapper;
    private final MethodicalPageContentMapper methodicalPageContentMapper;

    public PageDataDto getPageDataDto(UUID userId, UUID pageId){
        try{
            LessonPage page = lessonPageRepository.findById(pageId).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Страница не найдена не найден"));


            /// Тут должна быть проверка прав на страницу, но я не хочу поднимать все БД ради выдачи одной странички, это будет слишком медленно, Ладно я ее сделал
            Lesson lesson = lessonRepository.findById(page.getLessonId()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Урок не найден"));

            Course course = courseRepository.findById(lesson.getCourseId()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));

            if(!checkUserPermissions(course.getId(), userId)){
                log.error("User ID = {} opened course structure Course ID = {} - access error when download page ID = {}", userId, course.getId(), pageId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Курс недоступен");

            }

            MethodicalPageContent methodicalPageContent = methodicalPageContentRepository.findById(pageId).orElse(null);
            List<Question> questions = questionRepository.findByPageId(pageId);
            List<QuestionWithAnswersDto> questionWithAnswersDtoList = new ArrayList<>();
            List<QuestionOptionDto> questionOptionDtoList;
            List<QuestionTestCaseDto> questionTestCaseDtoList;

            for(Question que : questions){
                /// Тут можно оптимизировать и посмотреть на тип вопроса и не делать лишние поиски, я так и сделал
                questionOptionDtoList = null;
                questionTestCaseDtoList = null;
                switch (que.getType()){
                    case CODE:
                        questionTestCaseDtoList = questionTestCaseRepository.findByQuestionId(que.getId()).stream().map(questionTestCaseMapper::toDto).collect(Collectors.toList());
                        break;
                    case SINGLE_CHOICE:
                    case MULTIPLE_CHOICE:
                        questionOptionDtoList = questionOptionRepository.findByQuestionIdOrderBySortOrderAsc(que.getId()).stream().map(questionOptionMapper::toDto).collect(Collectors.toList());
                        break;
                    case TEXT_INPUT:
                        break;
                }

                questionWithAnswersDtoList.add(new QuestionWithAnswersDto(questionMapper.toDto(que), questionOptionDtoList, questionTestCaseDtoList));

            }

            return new PageDataDto(methodicalPageContentMapper.toDto(methodicalPageContent), questionWithAnswersDtoList);


        }catch (ResponseStatusException e) {
            throw e;
        }catch (Exception ex) {
            log.error("page data get  failed - server error, pageId={}", pageId, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось выдать страницу. Попробуйте позже."
            );
        }
    }

    private boolean checkUserPermissions(UUID courseId, UUID userId){
        /// Заглушка пока что.

        return true;
    }

}
