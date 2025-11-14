package ru.offer.hunt.oh_course.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.offer.hunt.oh_course.model.entity.MethodicalPageContent;

import java.util.UUID;

public interface MethodicalPageContentRepository
        extends JpaRepository<MethodicalPageContent, UUID> {}
