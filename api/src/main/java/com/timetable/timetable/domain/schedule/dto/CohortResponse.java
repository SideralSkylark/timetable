package com.timetable.timetable.domain.schedule.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.timetable.timetable.domain.schedule.entity.Cohort;

public record CohortResponse(
    Long id,
    int year,
    String section,
    int academicYear,
    int semester,
    Long courseId,
    String courseName,
    List<Long> studentIds
) {
    public static CohortResponse from(Cohort cohort) {
        return new CohortResponse(
            cohort.getId(),
            cohort.getYear(),
            cohort.getSection(),
            cohort.getAcademicYear(),
            cohort.getSemester(),
            cohort.getCourse().getId(),
            cohort.getCourse().getName(),
            cohort.getStudents().stream()
                .map(student -> student.getId())
                .collect(Collectors.toList())
        );
    }
}
