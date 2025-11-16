package ru.offer.hunt.oh_course.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class CollaboratorNotificationService {

    /**
     * Отправка уведомления/инвайта соавтору.
     */
    public void sendCollaboratorInvite(UUID courseId,
                                       UUID inviterId,
                                       UUID collaboratorId,
                                       String collaboratorEmail) {
        // Здесь просто логируем; никакой реальной отправки писем.
        log.info(
                "StubCollaboratorNotificationService: collaborator invite sent. " +
                        "courseId={}, inviterId={}, collaboratorId={}, email={}",
                courseId, inviterId, collaboratorId, collaboratorEmail
        );
    }
}
