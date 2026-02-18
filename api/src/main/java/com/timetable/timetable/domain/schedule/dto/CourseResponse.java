package com.timetable.timetable.domain.schedule.dto;

import java.time.LocalDateTime;

import com.timetable.timetable.domain.schedule.entity.Course;

public record CourseResponse (
    Long id,
    String name,
    Long coordinatorId,
    String coordinatorName,
    int years,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
            course.getId(),
            course.getName(),
            course.getCoordinator().getId(),
            course.getCoordinator().getUsername(),
            course.getYears(),
            course.getCreatedAt(),
            course.getUpdatedAt()
        );
    }
}
