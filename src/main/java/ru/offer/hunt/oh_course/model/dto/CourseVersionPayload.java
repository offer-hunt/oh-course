package ru.offer.hunt.oh_course.model.dto;

import lombok.Getter;
import lombok.Setter;
import ru.offer.hunt.oh_course.model.entity.Course;
import ru.offer.hunt.oh_course.model.enums.AccessType;
import ru.offer.hunt.oh_course.model.enums.CourseStatus;

@Getter
@Setter
public class CourseVersionPayload {

    private String title;
    private String description;
    private String coverUrl;
    private String language;
    private String level;
    private Integer estimatedDurationMin;
    private CourseStatus status;
    private AccessType accessType;
    private String inviteCode;
    private Boolean requiresEntitlement;
    private Integer maxFreeEnrollments;

    public static CourseVersionPayload fromEntity(Course course) {
        CourseVersionPayload payload = new CourseVersionPayload();
        payload.setTitle(course.getTitle());
        payload.setDescription(course.getDescription());
        payload.setCoverUrl(course.getCoverUrl());
        payload.setLanguage(course.getLanguage());
        payload.setLevel(course.getLevel());
        payload.setEstimatedDurationMin(course.getEstimatedDurationMin());
        payload.setStatus(course.getStatus());
        payload.setAccessType(course.getAccessType());
        payload.setInviteCode(course.getInviteCode());
        payload.setRequiresEntitlement(course.isRequiresEntitlement());
        payload.setMaxFreeEnrollments(course.getMaxFreeEnrollments());
        return payload;
    }

    public void applyToEntity(Course course) {
        course.setTitle(this.title);
        course.setDescription(this.description);
        course.setCoverUrl(this.coverUrl);
        course.setLanguage(this.language);
        course.setLevel(this.level);
        course.setEstimatedDurationMin(this.estimatedDurationMin);
        course.setStatus(this.status);
        course.setAccessType(this.accessType);
        course.setInviteCode(this.inviteCode);
        course.setRequiresEntitlement(this.requiresEntitlement);
        course.setMaxFreeEnrollments(this.maxFreeEnrollments);
    }
}
