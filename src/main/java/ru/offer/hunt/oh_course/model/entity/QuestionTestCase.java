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

import java.util.UUID;

@Entity
@Table(schema = "course", name = "course_question_test_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QuestionTestCase {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "input_data", nullable = false, columnDefinition = "text")
    private String inputData;

    @Column(name = "expected_output", nullable = false, columnDefinition = "text")
    private String expectedOutput;

    @Column(name = "timeout_ms")
    private Integer timeoutMs;

    @Column(name = "memory_limit_mb")
    private Integer memoryLimitMb;
}
