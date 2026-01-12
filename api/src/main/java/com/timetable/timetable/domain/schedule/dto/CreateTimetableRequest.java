package com.timetable.timetable.domain.schedule.dto;

import jakarta.validation.constraints.NotNull;

public record CreateTimetableRequest(
    @NotNull(message = "Academic period is required and cannot be empty")
    int academicYear,

    @NotNull(message = "Semester is required and cannot be empty")
    int semester
) {}
