package ru.offer.hunt.oh_course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.CourseGetDto;
import ru.offer.hunt.oh_course.model.dto.LessonWithPagesDto;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.mapper.*;
import ru.offer.hunt.oh_course.model.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseDataService {
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final LessonRepository lessonRepository;
    private final LessonPageRepository lessonPageRepository;
    private final LessonMapper lessonMapper;
    private final LessonPageMapper lessonPageMapper;

    @Transactional
    public CourseGetDto getCourseDto(UUID courseId, UUID userId){

        try{
            if(!checkUserPermissions(courseId, userId)){
                log.error("User ID = {} opened course structure Course ID = {} - access error", userId, courseId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Курс недоступен");

            }

            Course course = courseRepository.findById(courseId).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));

            List<Lesson> lessons = lessonRepository.findByCourseId(courseId);
            List<LessonWithPagesDto> lessonWithPagesDto = new ArrayList<>();
            for(Lesson ls : lessons){
                lessonWithPagesDto.add(new LessonWithPagesDto(lessonMapper.toDto(ls), lessonPageRepository.findByLessonId(ls.getId()).stream().map(lessonPageMapper::toDto).collect(Collectors.toList())));
            }

            return new CourseGetDto(courseMapper.toDto(course), lessonWithPagesDto);


        }catch (ResponseStatusException e) {
            throw e;
        }catch (Exception ex) {
            log.error("Course get structure failed - server error, courseId={}", courseId, ex);
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
