package ru.offer.hunt.oh_course.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.offer.hunt.oh_course.model.entity.TagRef;
import ru.offer.hunt.oh_course.model.repository.TagRefRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRefRepository tagRefRepository;

    @Transactional
    public List<TagRef> resolveTags(List<String> tagNames) {

        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        List<TagRef> tags = new ArrayList<>();

        for (String name : tagNames) {
            String normalized = name.trim().toLowerCase();
            TagRef tag = tagRefRepository
                    .findByNameIgnoreCase(normalized)
                    .orElseGet(() -> createTag(normalized));

            tags.add(tag);
        }

        return tags;
    }

    private TagRef createTag(String name) {
        TagRef tag = TagRef.builder()
                .id(UUID.randomUUID())
                .name(name)
                .createdAt(OffsetDateTime.now())
                .build();

        return tagRefRepository.save(tag);
    }
}
