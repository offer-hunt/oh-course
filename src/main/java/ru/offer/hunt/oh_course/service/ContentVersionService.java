package ru.offer.hunt.oh_course.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.ContentVersionDetailsDto;
import ru.offer.hunt.oh_course.model.dto.ContentVersionSummaryDto;
import ru.offer.hunt.oh_course.model.dto.CourseVersionPayload;
import ru.offer.hunt.oh_course.model.dto.LessonVersionPayload;
import ru.offer.hunt.oh_course.model.dto.VersionSaveRequest;
import ru.offer.hunt.oh_course.model.entity.ContentVersion;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.entity.Lesson;
import ru.offer.hunt.oh_course.model.enums.CourseMemberRole;
import ru.offer.hunt.oh_course.model.enums.VersionScope;
import ru.offer.hunt.oh_course.model.repository.ContentVersionRepository;
import ru.offer.hunt.oh_course.model.repository.CourseMemberRepository;
import ru.offer.hunt.oh_course.model.repository.CourseRepository;
import ru.offer.hunt.oh_course.model.repository.LessonRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentVersionService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CourseMemberRepository courseMemberRepository;
    private final ContentVersionRepository contentVersionRepository;
    private final ObjectMapper objectMapper;

    // Course versions

    @Transactional
    public ContentVersionDetailsDto saveCourseVersion(UUID courseId,
                                                      UUID userId,
                                                      VersionSaveRequest request) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Курс не найден"));

            ensureCourseAdmin(course.getId(), userId);

            CourseVersionPayload payload = CourseVersionPayload.fromEntity(course);
            String payloadJson = toJson(payload);

            OffsetDateTime now = OffsetDateTime.now();

            ContentVersion version = new ContentVersion();
            version.setId(UUID.randomUUID());
            version.setScope(VersionScope.COURSE);
            version.setCourseId(course.getId());
            version.setLessonId(null);
            version.setCreatedBy(userId);
            version.setCreatedAt(now);
            version.setComment(normalizeComment(request));
            version.setPayloadJson(payloadJson);

            ContentVersion saved = contentVersionRepository.save(version);

            log.info("Version saved: scope=COURSE, courseId={}, versionId={}, userId={}",
                    course.getId(), saved.getId(), userId);

            return toDetailsDto(saved);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Version save failed - server error, scope=COURSE, courseId={}, userId={}",
                    courseId, userId, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось сохранить версию."
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ContentVersionSummaryDto> getCourseVersions(UUID courseId,
                                                            UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Курс не найден"));

        ensureCourseAdmin(course.getId(), userId);

        List<ContentVersion> versions = contentVersionRepository
                .findByCourseIdAndScopeOrderByCreatedAtDesc(course.getId(), VersionScope.COURSE);

        return versions.stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContentVersionDetailsDto getCourseVersion(UUID courseId,
                                                     UUID versionId,
                                                     UUID userId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Курс не найден"));

        ensureCourseAdmin(course.getId(), userId);

        ContentVersion version = contentVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Версия не найдена"));

        if (!course.getId().equals(version.getCourseId())
                || version.getScope() != VersionScope.COURSE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Версия не относится к этому курсу"
            );
        }

        return toDetailsDto(version);
    }

    @Transactional
    public void restoreCourseVersion(UUID courseId,
                                     UUID versionId,
                                     UUID userId) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Курс не найден"));

            ensureCourseAdmin(course.getId(), userId);

            ContentVersion version = contentVersionRepository.findById(versionId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Версия не найдена"));

            if (!course.getId().equals(version.getCourseId())
                    || version.getScope() != VersionScope.COURSE) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Версия не относится к этому курсу"
                );
            }

            CourseVersionPayload payload =
                    fromJson(version.getPayloadJson(), CourseVersionPayload.class);

            payload.applyToEntity(course);
            course.setUpdatedAt(OffsetDateTime.now());

            courseRepository.save(course);

            log.info("Version restored: scope=COURSE, courseId={}, versionId={}, userId={}",
                    course.getId(), version.getId(), userId);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Version restore failed - server error, scope=COURSE, courseId={}, " +
                            "versionId={}, userId={}",
                    courseId, versionId, userId, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось восстановить версию."
            );
        }
    }

    // Lesson versions

    @Transactional
    public ContentVersionDetailsDto saveLessonVersion(UUID lessonId,
                                                      UUID userId,
                                                      VersionSaveRequest request) {
        try {
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Урок не найден"));

            UUID courseId = lesson.getCourseId();
            ensureCourseAdmin(courseId, userId);

            LessonVersionPayload payload = LessonVersionPayload.fromEntity(lesson);
            String payloadJson = toJson(payload);

            OffsetDateTime now = OffsetDateTime.now();

            ContentVersion version = new ContentVersion();
            version.setId(UUID.randomUUID());
            version.setScope(VersionScope.LESSON);
            version.setCourseId(courseId);
            version.setLessonId(lessonId);
            version.setCreatedBy(userId);
            version.setCreatedAt(now);
            version.setComment(normalizeComment(request));
            version.setPayloadJson(payloadJson);

            ContentVersion saved = contentVersionRepository.save(version);

            log.info("Version saved: scope=LESSON, courseId={}, lessonId={}, versionId={}, userId={}",
                    courseId, lessonId, saved.getId(), userId);

            return toDetailsDto(saved);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Version save failed - server error, scope=LESSON, lessonId={}, userId={}",
                    lessonId, userId, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось сохранить версию."
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ContentVersionSummaryDto> getLessonVersions(UUID lessonId,
                                                            UUID userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Урок не найден"));

        UUID courseId = lesson.getCourseId();
        ensureCourseAdmin(courseId, userId);

        List<ContentVersion> versions = contentVersionRepository
                .findByLessonIdAndScopeOrderByCreatedAtDesc(lessonId, VersionScope.LESSON);

        return versions.stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContentVersionDetailsDto getLessonVersion(UUID lessonId,
                                                     UUID versionId,
                                                     UUID userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Урок не найден"));

        UUID courseId = lesson.getCourseId();
        ensureCourseAdmin(courseId, userId);

        ContentVersion version = contentVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Версия не найдена"));

        if (!lesson.getId().equals(version.getLessonId())
                || version.getScope() != VersionScope.LESSON) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Версия не относится к этому уроку"
            );
        }

        return toDetailsDto(version);
    }

    @Transactional
    public void restoreLessonVersion(UUID lessonId,
                                     UUID versionId,
                                     UUID userId) {
        try {
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Урок не найден"));

            UUID courseId = lesson.getCourseId();
            ensureCourseAdmin(courseId, userId);

            ContentVersion version = contentVersionRepository.findById(versionId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Версия не найдена"));

            if (!lesson.getId().equals(version.getLessonId())
                    || version.getScope() != VersionScope.LESSON) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Версия не относится к этому уроку"
                );
            }

            LessonVersionPayload payload =
                    fromJson(version.getPayloadJson(), LessonVersionPayload.class);

            payload.applyToEntity(lesson);
            lesson.setUpdatedAt(OffsetDateTime.now());

            lessonRepository.save(lesson);

            log.info("Version restored: scope=LESSON, courseId={}, lessonId={}, versionId={}, userId={}",
                    courseId, lessonId, version.getId(), userId);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    "Version restore failed - server error, scope=LESSON, lessonId={}, versionId={}, userId={}",
                    lessonId, versionId, userId, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось восстановить версию."
            );
        }
    }

    private void ensureCourseAdmin(UUID courseId, UUID userId) {
        boolean allowed = courseMemberRepository
                .existsByIdCourseIdAndIdUserIdAndRoleIn(
                        courseId,
                        userId,
                        List.of(CourseMemberRole.OWNER, CourseMemberRole.ADMIN));

        if (!allowed) {
            log.warn("Version access forbidden: courseId={}, userId={}", courseId, userId);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Недостаточно прав для работы с версиями"
            );
        }
    }

    private String toJson(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    private <T> T fromJson(String json, Class<T> type) throws JsonProcessingException {
        return objectMapper.readValue(json, type);
    }

    private String normalizeComment(VersionSaveRequest request) {
        if (request == null || request.getComment() == null) {
            return null;
        }
        String trimmed = request.getComment().trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ContentVersionSummaryDto toSummaryDto(ContentVersion version) {
        ContentVersionSummaryDto dto = new ContentVersionSummaryDto();
        dto.setId(version.getId());
        dto.setScope(version.getScope());
        dto.setCourseId(version.getCourseId());
        dto.setLessonId(version.getLessonId());
        dto.setCreatedBy(version.getCreatedBy());
        dto.setCreatedAt(version.getCreatedAt());
        dto.setComment(version.getComment());
        return dto;
    }

    private ContentVersionDetailsDto toDetailsDto(ContentVersion version) {
        ContentVersionDetailsDto dto = new ContentVersionDetailsDto();
        dto.setId(version.getId());
        dto.setScope(version.getScope());
        dto.setCourseId(version.getCourseId());
        dto.setLessonId(version.getLessonId());
        dto.setCreatedBy(version.getCreatedBy());
        dto.setCreatedAt(version.getCreatedAt());
        dto.setComment(version.getComment());
        dto.setPayloadJson(version.getPayloadJson());
        return dto;
    }
}
