package ru.offer.hunt.oh_course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import ru.offer.hunt.oh_course.model.dto.LessonDto;
import ru.offer.hunt.oh_course.model.dto.LessonUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.mapper.LessonMapper;
import ru.offer.hunt.oh_course.model.repository.CourseRepository;
import ru.offer.hunt.oh_course.model.repository.LessonRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonService {
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final LessonMapper lessonMapper;

    @Transactional
    public LessonDto createLesson(
            UUID courseId,
            LessonUpsertRequest request,
            UUID userId
    ) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (!course.getAuthorId().equals(userId)) {
            throw new SecurityException("Нет прав на добавление урока в этот курс");
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.warn("Chapter add failed - empty title. CourseID: {}", courseId);
            throw new IllegalArgumentException("Название главы не может быть пустым");
        }

        try {
            List<Lesson> existingLessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);

            int nextOrderIndex;
            if (existingLessons.isEmpty()) {
                nextOrderIndex = 1;
            } else {
                Lesson lastLesson = existingLessons.get(existingLessons.size() - 1);
                nextOrderIndex = lastLesson.getOrderIndex() + 1;
            }

            Lesson lesson = lessonMapper.toEntity(courseId, request);

            lesson.setId(UUID.randomUUID());
            lesson.setOrderIndex(nextOrderIndex);

            OffsetDateTime now = OffsetDateTime.now();
            lesson.setCreatedAt(now);
            lesson.setUpdatedAt(now);
            lessonRepository.save(lesson);

            log.info("Chapter added. CourseID: {}, LessonID: {}", courseId, lesson.getId());

            return lessonMapper.toDto(lesson);

        } catch (Exception e) {
            log.error("Chapter add failed - server error. CourseID: {}", courseId, e);
            throw e;
        }
    }

    @Transactional
    public LessonDto updateLesson(UUID lessonId, LessonUpsertRequest request, UUID userId) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.warn("Chapter update failed - empty title. LessonID: {}", lessonId);
            throw new IllegalArgumentException("Название главы не может быть пустым");
        }

        try {
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new IllegalArgumentException("Lesson not found with id: " + lessonId));

            Course course = courseRepository.findById(lesson.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Course not found"));

            if (!course.getAuthorId().equals(userId)) {
                throw new SecurityException("Нет прав на редактирование этого урока");
            }

            Integer oldOrder = lesson.getOrderIndex();
            lessonMapper.update(lesson, request);

            lesson.setUpdatedAt(OffsetDateTime.now());
            lesson.setOrderIndex(oldOrder);

            lessonRepository.save(lesson);

            log.info("Lesson updated. ID: {}", lessonId);

            return lessonMapper.toDto(lesson);

        } catch (Exception e) {
            log.error("Chapter update failed - server error. LessonID: {}", lessonId, e);
            throw e;
        }
    }

    @Transactional
    public void deleteLesson(UUID lessonId, UUID userId) {
        try {
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new IllegalArgumentException("Урок не найден"));

            Course course = courseRepository.findById(lesson.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Курс урока не найден"));

            if (!course.getAuthorId().equals(userId)) {
                throw new SecurityException("Нет прав на удаление урока");
            }

            lessonRepository.delete(lesson);

            log.info("Lesson deleted. ID: {}", lessonId);

        } catch (Exception e) {
            log.error("Lesson deletion failed - server error. ID: {}", lessonId, e);
            throw e;
        }
    }

}
