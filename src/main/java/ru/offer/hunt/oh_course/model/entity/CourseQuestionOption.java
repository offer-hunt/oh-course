package ru.offer.hunt.oh_course.model.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = "course", name = "course_question_options")
@Getter
@Setter
public class CourseQuestionOption {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private CourseQuestion question;

    @Column(nullable = false, columnDefinition = "text")
    private String label;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
