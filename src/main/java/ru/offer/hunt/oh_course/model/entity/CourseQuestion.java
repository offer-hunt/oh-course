package ru.offer.hunt.oh_course.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.enums.QuestionType;

@Entity
@Table(schema = "course", name = "course_questions")
@Getter
@Setter
public class CourseQuestion {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private LessonPage page;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    // ВАЖНО: correctAnswer в view не отдаём, но в БД он есть.
    @Column(name = "correct_answer", columnDefinition = "text")
    private String correctAnswer;

    @Column(name = "use_ai_check", nullable = false)
    private boolean useAiCheck;

    private Integer points;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
