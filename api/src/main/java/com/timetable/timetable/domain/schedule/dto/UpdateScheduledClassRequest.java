package com.timetable.timetable.domain.schedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public record UpdateScheduledClassRequest(
    @NotNull(message = "cohort subject id is required")
    Long cohortSubjectId,

    Long timetableId,
    
    @NotNull(message = "room ID is required")
    Long roomId,
    
    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date cannot be in the past")
    LocalDate date,
    
    @NotNull(message = "Start time is required")
    LocalTime startTime,
    
    @NotNull(message = "End time is required")
    LocalTime endTime
) {}
