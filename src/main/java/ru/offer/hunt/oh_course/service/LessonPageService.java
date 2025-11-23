package ru.offer.hunt.oh_course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.offer.hunt.oh_course.model.dto.LessonPageDto;
import ru.offer.hunt.oh_course.model.dto.LessonPageUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.entity.LessonPage;
import ru.offer.hunt.oh_course.model.mapper.LessonPageMapper;
import ru.offer.hunt.oh_course.model.repository.LessonPageRepository;
import ru.offer.hunt.oh_course.model.repository.LessonRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonPageService {
    private final LessonPageRepository lessonPageRepository;
    private final LessonPageMapper lessonPageMapper;
    private final LessonRepository lessonRepository;

    @Transactional
    public LessonPageDto addLessonPage(UUID lessonId, LessonPageUpsertRequest request) {
        String pageTypeLogName = request.getPageType() != null ? request.getPageType().name() : "Unknown";

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.warn("{} add failed - empty title. LessonID: {}", pageTypeLogName, lessonId);
            throw new IllegalArgumentException("Название страницы не может быть пустым");
        }

        try {
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new IllegalArgumentException("Lesson not found with id: " + lessonId));
            UUID courseId = lesson.getCourseId();

            List<LessonPage> existingPages = lessonPageRepository.findByLessonIdOrderBySortOrderAsc(lessonId);
            int nextOrderIndex;
            if (existingPages.isEmpty()) {
                nextOrderIndex = 1;
            } else {
                LessonPage lastPage = existingPages.get(existingPages.size() - 1);
                nextOrderIndex = lastPage.getSortOrder() + 1;
            }

            LessonPage lessonPage = lessonPageMapper.toEntity(lessonId, request);
            lessonPage.setId(UUID.randomUUID());
            lessonPage.setSortOrder(nextOrderIndex);

            OffsetDateTime now = OffsetDateTime.now();
            lessonPage.setCreatedAt(now);
            lessonPage.setUpdatedAt(now);

            lessonPageRepository.save(lessonPage);

            log.info("{} page added. CourseID: {}, LessonID: {}", pageTypeLogName, courseId, lessonId);

            return lessonPageMapper.toDto(lessonPage);

        } catch (Exception e) {
            log.error("{} page add failed - server error. LessonID: {}", pageTypeLogName, lessonId, e);
            throw e;
        }
    }

    @Transactional
    public LessonPageDto addLessonToChapter(
            UUID chapterId,
            LessonPageUpsertRequest request
    ) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.warn("Lesson add failed - empty title. СhapterId: {}", chapterId);
            throw new IllegalArgumentException("Название урока не может быть пустым");
        }

        try {
            Lesson chapter = lessonRepository.findById(chapterId)
                    .orElseThrow(() -> new IllegalArgumentException("Chapter (Lesson) not found with id: " + chapterId));
            UUID courseId = chapter.getCourseId();

            List<LessonPage> existingItems = lessonPageRepository.findByLessonIdOrderBySortOrderAsc(chapterId);
            int nextOrderIndex;
            if (existingItems.isEmpty()) {
                nextOrderIndex = 1;
            } else {
                LessonPage lastItem = existingItems.get(existingItems.size() - 1);
                nextOrderIndex = lastItem.getSortOrder() + 1;
            }

            LessonPage lessonPage = lessonPageMapper.toEntity(chapterId, request);
            lessonPage.setId(UUID.randomUUID());
            lessonPage.setSortOrder(nextOrderIndex);

            OffsetDateTime now = OffsetDateTime.now();
            lessonPage.setCreatedAt(now);
            lessonPage.setUpdatedAt(now);

            lessonPageRepository.save(lessonPage);

            log.info("Lesson added. CourseID: {}, LessonID: {}", courseId, lessonPage.getId());

            return lessonPageMapper.toDto(lessonPage);

        } catch (Exception e) {
            log.error("Lesson add failed - server error. ChapterID: {}", chapterId, e);
            throw e;
        }
    }

    @Transactional
    public LessonPageDto updateLessonPage(
            UUID pageId,
            LessonPageUpsertRequest request
    ) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.warn("Page update failed - empty title. PageID: {}", pageId);
            throw new IllegalArgumentException("Название страницы не может быть пустым");
        }

        try {
            LessonPage lessonPage = lessonPageRepository.findById(pageId)
                    .orElseThrow(() -> new IllegalArgumentException("Page not found with id: " + pageId));

            Integer oldOrder = lessonPage.getSortOrder();
            lessonPageMapper.update(lessonPage, request);

            lessonPage.setUpdatedAt(OffsetDateTime.now());
            lessonPage.setSortOrder(oldOrder);

            lessonPageRepository.save(lessonPage);

            log.info("Page updated. ID: {}", pageId);

            return lessonPageMapper.toDto(lessonPage);

        } catch (Exception e) {
            log.error("Page update failed - server error. PageID: {}", pageId, e);
            throw e;
        }
    }

    @Transactional
    public void deleteLessonPage(UUID pageId) {
        if (!lessonPageRepository.existsById(pageId)) {
            log.warn("Attempt to delete non-existent page: {}", pageId);
            throw new IllegalArgumentException("Не найдена страница. ID: " + pageId);
        }
        try {
            lessonPageRepository.deleteById(pageId);
            log.info("Page deleted. ID: {}", pageId);
        } catch (Exception e) {
            log.error("Page delete failed - server error. ID: {}", pageId, e);
            throw e;
        }
    }
}
