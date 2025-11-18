package com.timetable.timetable.domain.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCourseRequest(
    @NotBlank(message = "Course name must not be empty.") 
    String name,

    @NotNull(message = "Course coordinator not specified.")
    @Positive(message = "Coordinator ID must be positive.")
    Long coordinatorId
) { }
