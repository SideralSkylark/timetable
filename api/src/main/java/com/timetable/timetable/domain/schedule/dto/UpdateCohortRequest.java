package com.timetable.timetable.domain.schedule.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCohortRequest(
    @NotNull(message = "students year must be specified")
    @Positive(message = "students year must be positive")
    int year,

    @NotBlank(message = "section must not be empty")
    String section,

    @NotNull(message = "academic year must be specified")
    @Positive(message = "academic year must be positive")
    int academicYear,
    
    @NotEmpty(message = "At least one student must be assigned to the cohort")
    List<Long> studentIds
) {}
