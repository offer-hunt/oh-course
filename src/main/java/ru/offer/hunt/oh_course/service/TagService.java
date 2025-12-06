package ru.offer.hunt.oh_course.service;

import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.offer.hunt.oh_course.model.entity.TagRef;
import ru.offer.hunt.oh_course.model.repository.TagRefRepository;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRefRepository tagRefRepository;

    /**
     * tags == null  -> не трогаем теги (для update)
     * tags empty    -> очищаем теги
     * иначе         -> upsert по имени (case-insensitive)
     */
    @Transactional
    public List<TagRef> resolveTagRefs(List<String> tags) {
        if (tags == null) return null;

        // trim + remove blanks + dedupe ignore-case preserving order
        Map<String, String> uniq = new LinkedHashMap<>();
        for (String t : tags) {
            if (t == null) continue;
            String v = t.trim();
            if (v.isEmpty()) continue;
            uniq.putIfAbsent(v.toLowerCase(Locale.ROOT), v);
        }

        if (uniq.isEmpty()) return List.of();

        OffsetDateTime now = OffsetDateTime.now();
        List<TagRef> result = new ArrayList<>(uniq.size());

        for (String original : uniq.values()) {
            TagRef ref = tagRefRepository.findByNameIgnoreCase(original)
                    .orElseGet(() -> {
                        TagRef created = TagRef.builder()
                                .id(UUID.randomUUID())
                                .name(original)
                                .createdAt(now)
                                .build();
                        return tagRefRepository.save(created);
                    });
            result.add(ref);
        }

        return result;
    }
}
