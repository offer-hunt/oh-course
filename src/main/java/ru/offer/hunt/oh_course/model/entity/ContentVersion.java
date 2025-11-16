package ru.offer.hunt.oh_course.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.VersionScope;

@Entity
@Table(name = "course_content_versions", schema = "course")
@Getter
@Setter
public class ContentVersion {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "lesson_id")
    private UUID lessonId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private VersionScope scope;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "comment")
    private String comment;

    @Column(name = "payload_json", nullable = false, columnDefinition = "text")
    private String payloadJson;
}
