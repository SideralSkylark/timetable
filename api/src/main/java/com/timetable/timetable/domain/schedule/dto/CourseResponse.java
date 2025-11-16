package com.timetable.timetable.domain.schedule.dto;

import java.time.LocalDateTime;

import com.timetable.timetable.domain.schedule.entity.Course;

public record CourseRespose (
    Long id,
    String name,
    Long coordinatorId,
    LocalDateTime createdAt
) {
    public static CourseRespose from(Course course) {
        return new CourseRespose(
            course.getId(),
            course.getName(),
            course.getCoordinator().getId(),
            course.getCreatedAt()
        );
    }
}
