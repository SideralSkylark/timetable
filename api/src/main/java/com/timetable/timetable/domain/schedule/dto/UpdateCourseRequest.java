package com.timetable.timetable.domain.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCourseRequest(
    @NotBlank(message = "Course name must not be empty.") 
    String name,

    @NotNull(message = "Corse coordinator no specified.")
    Long coordinatorId
) {
}
