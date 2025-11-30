package ru.offer.hunt.oh_course.model.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseFilter {
    private UUID authorId;
    private List<String> language;
    private List<String> technologies;
    private List<String> level;
    private List<Integer> duration;
    private String query;
}
