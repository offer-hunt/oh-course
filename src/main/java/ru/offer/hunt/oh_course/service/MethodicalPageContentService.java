package ru.offer.hunt.oh_course.service;


import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentDto;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.MethodicalPageContent;
import ru.offer.hunt.oh_course.model.mapper.MethodicalPageContentMapper;
import ru.offer.hunt.oh_course.model.repository.LessonPageRepository;
import ru.offer.hunt.oh_course.model.repository.MethodicalPageContentRepository;

import java.beans.Transient;
import java.util.UUID;



@AllArgsConstructor
@Service
public class MethodicalPageContentService {
    private final MethodicalPageContentRepository methodicalPageContentRepository;
    private final MethodicalPageContentMapper methodicalPageContentMapper;
    private final LessonPageRepository lessonPageRepository;

    private final int MAX_TEXT_LENGTH = 1000000;

    @Transactional(readOnly = true)
    public MethodicalPageContentDto get(UUID id){
        var entity = methodicalPageContentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Methodical page not found"));

        return methodicalPageContentMapper.toDto(entity);

    }


    @Transactional
    public MethodicalPageContentDto create(UUID pageId, MethodicalPageContentUpsertRequest upsertRequest){
        if(!lessonPageRepository.existsById(pageId)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson page not found");
        }

        if (methodicalPageContentRepository.existsById(pageId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Content already exists for this page");
        }

//        if (upsertRequest.getMarkdown().length() > MAX_TEXT_LENGTH) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Слишком длинный текст");
//        }

        MethodicalPageContent page = methodicalPageContentMapper.toEntity(pageId, upsertRequest);

        return null;

    }


}
