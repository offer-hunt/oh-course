package ru.offer.hunt.oh_course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.offer.hunt.oh_course.model.dto.CourseDto;
import ru.offer.hunt.oh_course.model.dto.CourseStatsDto;
import ru.offer.hunt.oh_course.model.dto.CourseUpsertRequest;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;
import ru.offer.hunt.oh_course.model.search.CourseFilter;
import ru.offer.hunt.oh_course.security.SecurityUtils;
import ru.offer.hunt.oh_course.service.CourseService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Courses", description = "Публичные и авторские операции с курсами")
public class CourseController {

    private final CourseService courseService;

    @Operation(
            summary = "Получить список опубликованных курсов",
            description = """
                    возвращает список курсов в статусе PUBLISHED \
                    с поддержкой фильтров по языку, технологиям, уровню, длительности и поисковой строке."""
    )
    @GetMapping
    public List<CourseDto> getPublishedCourses(
            @Parameter(description = "Язык программирования")
            @RequestParam(value = "language", required = false) List<String> language,
            @Parameter(description = "Технологии (теги)")
            @RequestParam(value = "technologies", required = false) List<String> technologies,
            @Parameter(description = "Сложность")
            @RequestParam(value = "level", required = false) List<String> level,
            @Parameter(description = "Длительность в часах (с допуском)")
            @RequestParam(value = "duration", required = false) List<Integer> duration,
            @Parameter(description = "Поисковый запрос по названию/описанию/тегам")
            @RequestParam(value = "query", required = false) String query,
            @Parameter(description = "Фильтрация по authorId (опционально)")
            @RequestParam(value = "authorId", required = false) UUID authorId
    ) {
        log.debug("Get courses request");
        CourseFilter filter = new CourseFilter(authorId, language, technologies, level, duration, query);
        return courseService.getPublishedCourses(filter);
    }

    @Operation(
            summary = "Получить опубликованный курс по slug",
            description = """
                    возвращает полное описание опубликованного курса по его slug. \
                    Для курсов с типом доступа PRIVATE_LINK необходимо передать корректный inviteCode."""
    )
    @GetMapping("/{slug}")
    public CourseDto getPublishedCourseBySlug(
            @PathVariable String slug,
            @RequestParam(value = "inviteCode", required = false) String inviteCode
    ) {
        log.debug("Get course by slug request: slug='{}'", slug);
        return courseService.getPublishedCourseBySlug(slug, inviteCode);
    }

    @Operation(
            summary = "Создать новый курс (черновик)",
            description = """
                    Авторский сценарий: создаёт черновой курс от имени текущего пользователя. \
                    Пользователь автоматически становится владельцем (OWNER), создаётся базовая статистика по курсу."""
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDto createCourse(@Valid @RequestBody CourseUpsertRequest request,
                                  JwtAuthenticationToken authentication) {

        UUID authorId = SecurityUtils.getUserId(authentication);

        log.debug("Create course request: title='{}', authorId={}",
                request.getTitle(), authorId);

        return courseService.createCourse(authorId, request);
    }

    @Operation(
            summary = "Опубликовать курс",
            description = """
                    Переводит курс в статус PUBLISHED. \
                    Доступно только для участников курса с ролью OWNER/ADMIN. \
                    Перед публикацией проверяется наличие обязательных полей и хотя бы одного урока с контентом."""
    )
    @PostMapping("/{courseId}/publish")
    public CourseDto publishCourse(
            @PathVariable UUID courseId,
            JwtAuthenticationToken authentication) {

        UUID userId = SecurityUtils.getUserId(authentication);
        return courseService.publishCourse(courseId, userId);
    }

    @Operation(
            summary = "Получить несколько курсов по списку идентификаторов",
            description = """
                    Вспомогательный эндпоинт для других сервисов (например, Learning) и фронта. \
                    По списку id курсов возвращает их полное представление CourseDto."""
    )
    @GetMapping("/batch")
    public List<CourseDto> getCoursesBatch(
            @RequestParam("ids") List<UUID> ids
    ) {
        log.debug("Get courses batch: ids={}", ids);
        return courseService.getCoursesByIds(ids);
    }

    @Operation(
            summary = "Получить мои курсы как автора/админа",
            description = """
                    возвращает курсы, где текущий пользователь является \
                    участником с ролью OWNER или ADMIN. Можно фильтровать по статусам курса."""
    )
    @GetMapping("/my")
    public List<CourseDto> getMyCourses(
            @RequestParam(value = "status", required = false) List<CourseStatus> statuses,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        log.debug("Get my courses: userId={}, statuses={}", userId, statuses);
        return courseService.getMyCourses(userId, statuses);
    }

    @Operation(
            summary = "Получить детали курса по id",
            description = """
                    Внутренний/служебный эндпоинт: возвращает CourseDto по идентификатору курса. \
                    Удобен для интеграций других сервисов и сценариев, где известен только courseId."""
    )
    @GetMapping("/{courseId}/details")
    public CourseDto getCourseDetails(
            @PathVariable UUID courseId
    ) {
        log.debug("Get course details by id: {}", courseId);
        return courseService.getCourseDetails(courseId);
    }

    @Operation(
            summary = "Получить агрегированную статистику курса",
            description = """
                    детальная статистика для автора/админа курса \
                    (количество записей, средний прогресс прохождения, средний рейтинг). \
                    Доступ только участникам курса с ролью OWNER/ADMIN."""
    )
    @GetMapping("/{courseId}/stats")
    public CourseStatsDto getCourseStats(
            @PathVariable UUID courseId,
            JwtAuthenticationToken authentication
    ) {
        UUID userId = SecurityUtils.getUserId(authentication);
        log.debug("Get course stats: courseId={}, userId={}", courseId, userId);
        return courseService.getCourseStats(courseId, userId);
    }
}
