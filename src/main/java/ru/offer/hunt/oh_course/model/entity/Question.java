package ru.offer.hunt.oh_course.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.QuestionType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "course", name = "course_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Question {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "page_id", nullable = false)
    private UUID pageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private QuestionType type;

    @Column(name = "text", nullable = false, columnDefinition = "text")
    private String text;

    @Column(name = "correct_answer", columnDefinition = "text")
    private String correctAnswer;

    @Column(name = "use_ai_check", nullable = false)
    private boolean useAiCheck;

    @Column(name = "points")
    private Integer points;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
