package ru.offer.hunt.oh_course.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "course", name = "course_methodical_page_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
