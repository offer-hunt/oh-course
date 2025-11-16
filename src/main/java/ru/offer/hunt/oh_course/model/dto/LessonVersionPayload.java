package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.entity.Lesson;

@Getter
@Setter
public class LessonVersionPayload {

    private String title;
    private String description;
    private Integer orderIndex;
    private Integer durationMin;

    public static LessonVersionPayload fromEntity(Lesson lesson) {
        LessonVersionPayload payload = new LessonVersionPayload();
        payload.setTitle(lesson.getTitle());
        payload.setDescription(lesson.getDescription());
        payload.setOrderIndex(lesson.getOrderIndex());
        payload.setDurationMin(lesson.getDurationMin());
        return payload;
    }

    public void applyToEntity(Lesson lesson) {
        lesson.setTitle(this.title);
        lesson.setDescription(this.description);
        lesson.setOrderIndex(this.orderIndex);
        lesson.setDurationMin(this.durationMin);
    }
}
