package ru.offer.hunt.oh_course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.offer.hunt.oh_course.model.dto.LessonDto;
import ru.offer.hunt.oh_course.model.dto.LessonUpsertRequest;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentDto;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.entity.MethodicalPageContent;
import ru.offer.hunt.oh_course.model.mapper.MethodicalPageContentMapper;
import ru.offer.hunt.oh_course.model.repository.MethodicalPageContentRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MethodicalPageContentService {
    private final MethodicalPageContentRepository methodicalPageContentRepository;
    private final MethodicalPageContentMapper methodicalPageContentMapper;

    @Transactional
    public ResponseEntity<MethodicalPageContentDto> addPageContent(MethodicalPageContentUpsertRequest request) {
        return null;
    }

    @Transactional
    public MethodicalPageContentDto savePageContent(
            UUID pageId,
            MethodicalPageContentUpsertRequest request
    ) {
        return null;
//        if (request.getMarkdown() == null || request.getMarkdown().trim().isEmpty()) {
//            log.warn("Chapter add failed - empty title. CourseID: {}", courseId);
//            throw new IllegalArgumentException("Название главы не может быть пустым");
//        }
//
//        try {
//            MethodicalPageContent methodicalPageContent = methodicalPageContentMapper.toEntity(pageId, request);
//
//            methodicalPageContent.setPageId(pageId);
//            methodicalPageContent.setMarkdown(request.getMarkdown());
//            methodicalPageContent.setExternalVideoUrl(request.getExternalVideoUrl());
//
//            OffsetDateTime now = OffsetDateTime.now();
//            methodicalPageContent.setUpdatedAt(now);
//
//            methodicalPageContentRepository.save(methodicalPageContent);
//
//            log.info("Chapter added. CourseID: {}, LessonID: {}", courseId, methodicalPageContent.getId());
//
//            return methodicalPageContentMapper.toDto(methodicalPageContent);
//
//        } catch (Exception e) {
//            log.error("Chapter add failed - server error. CourseID: {}", courseId, e);
//            throw e;
//        }
    }
}
