package com.timetable.timetable.domain.schedule.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCourseRequest(
    @NotBlank(message = "Course name must not be empty.") 
    String name,

    @NotBlank(message = "Corse coordinator no specified.")
    Long coordinatorId
) {
}
