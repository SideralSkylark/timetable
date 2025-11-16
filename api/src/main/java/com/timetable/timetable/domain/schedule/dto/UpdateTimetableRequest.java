package com.timetable.timetable.domain.schedule.dto;

import com.timetable.timetable.domain.schedule.entity.TimetableStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateTimetableRequest(
    @NotBlank(message = "Academic period is required and cannot be empty")
    @Size(min = 4, max = 50, message = "Academic period must be between 4 and 50 characters")
    @Pattern(
        regexp = "^[0-9]{4}\\.[1-2]$|^(Spring|Summer|Fall|Winter)\\s[0-9]{4}$",
        message = "Academic period must follow format '2024.1' or 'Fall 2024'"
    )
    String academicPeriod,
    
    @NotNull(message = "Status is required")
    TimetableStatus status
) {}
