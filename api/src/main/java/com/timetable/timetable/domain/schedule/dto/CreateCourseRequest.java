package com.timetable.timetable.domain.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCourseRequest(
    @NotBlank(message = "Course name must not be empty.") 
    String name,
  
    @NotNull(message = "students year must be specified")
    @Positive(message = "students year must be positive")
    int year,

    @NotBlank(message = "section must not be empty")
    String section,

    @NotNull(message = "academic year must be specified")
    @Positive(message = "academic year must be positive")
    int academicYear,

    @NotNull(message = "Course coordinator not specified.")
    @Positive(message = "Coordinator ID must be positive.")
    Long coordinatorId
) { }
