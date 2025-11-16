package ru.offer.hunt.oh_course.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.offer.hunt.oh_course.model.dto.CourseMemberDto;
import ru.offer.hunt.oh_course.model.dto.CourseMemberUpsertRequest;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.entity.CourseMember;
import ru.offer.hunt.oh_course.model.enums.CourseMemberRole;
import ru.offer.hunt.oh_course.model.id.CourseMemberId;
import ru.offer.hunt.oh_course.model.repository.CourseMemberRepository;
import ru.offer.hunt.oh_course.model.repository.CourseRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseMemberService {

    private final CourseRepository courseRepository;
    private final CourseMemberRepository courseMemberRepository;
    private final UserDirectoryClient userDirectoryClient;
    private final CollaboratorNotificationService collaboratorNotificationService;

    /**
     * Добавление соавтора (Автор/Модератор) в курс.
     */
    @Transactional
    public CourseMemberDto addCollaborator(UUID courseId,
                                           UUID currentUserId,
                                           CourseMemberUpsertRequest request) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Курс не найден"));

            ensureCanManageMembers(course.getId(), currentUserId);

            String email = normalizeEmail(request.getEmail());

            UserDirectoryClient.UserInfo targetUser = userDirectoryClient
                    .findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Collaborator add failed - user not found: courseId={}, email={}, byUserId={}",
                                course.getId(), email, currentUserId);
                        return new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Пользователь с таким email не найден"
                        );
                    });

            if (courseMemberRepository.existsByIdCourseIdAndIdUserId(course.getId(), targetUser.getId())) {
                log.warn("Collaborator add failed - user already added: courseId={}, targetUserId={}, byUserId={}",
                        course.getId(), targetUser.getId(), currentUserId);
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Этот пользователь уже является соавтором"
                );
            }

            OffsetDateTime now = OffsetDateTime.now();

            CourseMemberId memberId = new CourseMemberId(course.getId(), targetUser.getId());
            CourseMember member = CourseMember.builder()
                    .id(memberId)
                    .role(request.getRole())
                    .addedAt(now)
                    .addedBy(currentUserId)
                    .build();

            CourseMember saved = courseMemberRepository.save(member);

            collaboratorNotificationService.sendCollaboratorInvite(
                    course.getId(),
                    currentUserId,
                    targetUser.getId(),
                    targetUser.getEmail()
            );

            log.info(
                    "Collaborator added: courseId={}, collaboratorId={}, role={}, addedBy={}",
                    course.getId(),
                    targetUser.getId(),
                    saved.getRole(),
                    currentUserId
            );

            CourseMemberDto dto = new CourseMemberDto();
            dto.setCourseId(course.getId());
            dto.setUserId(targetUser.getId());
            dto.setRole(saved.getRole());
            dto.setAddedAt(saved.getAddedAt());
            dto.setAddedBy(saved.getAddedBy());

            return dto;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    "Collaborator add failed - server error, courseId={}, byUserId={}, email={}",
                    courseId,
                    currentUserId,
                    request.getEmail(),
                    e
            );
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось добавить соавтора. Попробуйте позже.",
                    e
            );
        }
    }

    private void ensureCanManageMembers(UUID courseId, UUID userId) {
        boolean allowed = courseMemberRepository
                .existsByIdCourseIdAndIdUserIdAndRoleIn(
                        courseId,
                        userId,
                        List.of(CourseMemberRole.OWNER, CourseMemberRole.ADMIN)
                );

        if (!allowed) {
            log.warn("Collaborator add forbidden: courseId={}, userId={}", courseId, userId);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Недостаточно прав для управления ролями в курсе"
            );
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
