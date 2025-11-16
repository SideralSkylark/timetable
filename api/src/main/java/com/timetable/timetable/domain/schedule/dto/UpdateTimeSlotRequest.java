package com.timetable.timetable.domain.schedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public record UpdateTimeSlotRequest(
    @NotNull(message = "Subject ID is required")
    Long subjectId,
    
    @NotNull(message = "Teacher ID is required")
    Long teacherId,
    
    @NotNull(message = "Room ID is required")
    Long roomId,
    
    @NotNull(message = "Cohort ID is required")
    Long cohortId,
    
    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date cannot be in the past")
    LocalDate date,
    
    @NotNull(message = "Start time is required")
    LocalTime startTime,
    
    @NotNull(message = "End time is required")
    LocalTime endTime
) {}
