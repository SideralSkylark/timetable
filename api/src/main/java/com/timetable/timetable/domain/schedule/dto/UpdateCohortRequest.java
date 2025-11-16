package com.timetable.timetable.domain.schedule.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UpdateCohortRequest(
    @NotBlank(message = "Cohort name is required and cannot be empty")
    @Size(min = 2, max = 100, message = "Cohort name must be between 2 and 100 characters")
    String name,
    
    @NotEmpty(message = "At least one student must be assigned to the cohort")
    List<Long> studentIds
) {}
