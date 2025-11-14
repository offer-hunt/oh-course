package ru.offer.hunt.oh_course.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.offer.hunt.oh_course.model.entity.TagRef;

import java.util.Optional;
import java.util.UUID;

public interface TagRefRepository extends JpaRepository<TagRef, UUID> {

    Optional<TagRef> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
