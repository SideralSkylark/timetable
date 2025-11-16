package com.timetable.timetable.domain.schedule.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCohortRequest(
    @NotBlank(message = "Cohort name is required and cannot be empty")
    @Size(min = 2, max = 100, message = "Cohort name must be between 2 and 100 characters")
    String name,
    
    @NotNull(message = "Course ID is required")
    Long courseId,
    
    List<Long> studentIds
) {}
