package com.timetable.timetable.domain.schedule.dto;

import com.timetable.timetable.domain.schedule.entity.TimetableStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateTimetableRequest(
    @NotNull(message = "academic year is required")
    int academicYear,

    @NotNull(message = "semester is required")
    int semester,
    
    @NotNull(message = "Status is required")
    TimetableStatus status
) {}
