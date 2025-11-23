//package ru.offer.hunt.oh_course.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentDto;
//import ru.offer.hunt.oh_course.model.dto.MethodicalPageContentUpsertRequest;
//import ru.offer.hunt.oh_course.service.MethodicalPageContentService;
//
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/pages/{pageId}/content")
//@RequiredArgsConstructor
//@Tag(name = "Контент страниц", description = "Управление содержимым (Markdown, Видео)")
//public class MethodicalPageContentController {
//    private final MethodicalPageContentService methodicalPageContentService;
//
//    @PutMapping("/methodical")
//    @Operation(summary = "Сохранить/Обновить теорию", description = "Сохраняет Markdown и видео для теоретической страницы.")
//    public ResponseEntity<MethodicalPageContentDto> upsertMethodicalContent(
//            @PathVariable UUID pageId,
//            @Valid @RequestBody MethodicalPageContentUpsertRequest request
//    ) {
//        MethodicalPageContentDto content = methodicalPageContentService.savePageContent(pageId, request);
//        return ResponseEntity.ok(content);
//    }
//}
