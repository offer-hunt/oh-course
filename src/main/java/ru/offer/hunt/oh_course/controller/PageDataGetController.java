package ru.offer.hunt.oh_course.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.offer.hunt.oh_course.model.dto.PageDataDto;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.PageDataGetService;

import java.util.UUID;

@RestController
@RequestMapping("/api/coursesDataPage")
@RequiredArgsConstructor
@Slf4j
public class PageDataGetController {
    private final PageDataGetService pageDataGetService;

    @GetMapping("/{pageId}")
    public PageDataDto getDataPage(@PathVariable("pageId") UUID pageId, JwtAuthenticationToken authentication) {
        UUID userId = SecurityUtils.getUserId(authentication);
        return pageDataGetService.getPageDataDto(userId, pageId);
    }
}
