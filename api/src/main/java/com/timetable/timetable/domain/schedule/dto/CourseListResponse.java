package com.timetable.timetable.domain.schedule.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.timetable.timetable.domain.schedule.entity.Course;

public record CourseListResponse(
        Long id,
        String name,
        Long coordinatorId,
        String coordinatorName,
        int years,
        Map<Integer, Integer> expectedCohortsPerYear,
        Long subjectCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static CourseListResponse from(Course course, Long subjectCount) {
        return new CourseListResponse(
                course.getId(),
                course.getName(),
                course.getCoordinator().getId(),
                course.getCoordinator().getUsername(),
                course.getYears(),
                course.getExpectedCohortsPerAcademicYear(),
                subjectCount,
                course.getCreatedAt(),
                course.getUpdatedAt());
    }
}
