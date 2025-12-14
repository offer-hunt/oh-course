package ru.offer.hunt.oh_course.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = "course", name = "course_methodical_page_content")
@Getter
@Setter
public class MethodicalPageContent {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "page_id", nullable = false)
    private UUID pageId;

    @Column(name = "markdown", nullable = false, columnDefinition = "text")
    private String markdown;

    @Column(name = "external_video_url", columnDefinition = "text")
    private String externalVideoUrl;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
