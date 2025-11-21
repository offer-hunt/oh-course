package ru.offer.hunt.oh_course.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentDto;
import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentUpsertRequest;
import ru.offer.hunt.oh_course.model.mapper.MethodicalPageContentMapper;
import ru.offer.hunt.oh_course.model.repository.LessonPageRepository;
import ru.offer.hunt.oh_course.model.repository.MethodicalPageContentRepository;

import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MethodicalPageContentService {

    private final MethodicalPageContentMapper methodicalPageContentMapper;
    private final MethodicalPageContentRepository methodicalPageContentRepository;
    private final LessonPageRepository lessonPageRepository;
    private final static Integer MAX_SIZE = 100000;

    @Transactional
    public MethodicalPageContentDto create(UUID pageId, MethodicalPageContentUpsertRequest methodicalPageContentUpsertRequest){

        /// Проверяем существование страницы
        if(!lessonPageRepository.existsById(pageId)){
            log.error("Lesson Page for methodical page content not found, pageID = {}", pageId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson Page for methodical page content not found");
        }

        /// Проверка длины, но лучше заменить на проверку по размеру файла, так мы будем учитывать кодировку
        if(methodicalPageContentUpsertRequest.getMarkdown().length() > MAX_SIZE){
            log.error("Markdown length is too long, length = {}", methodicalPageContentUpsertRequest.getMarkdown().length());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Markdown length is is too long");
        }


        var methodicalPage = methodicalPageContentMapper.toEntity(pageId, methodicalPageContentUpsertRequest);
        methodicalPage.setUpdatedAt(null);

        try{
            methodicalPage = methodicalPageContentRepository.save(methodicalPage);
            return methodicalPageContentMapper.toDto(methodicalPage);
        }catch (Exception e){

            log.error("Не сохранили объект - мета данные на странице с id = {}", pageId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не получилось сохранить мета данные, попробуйте позже");

        }
    }
}
