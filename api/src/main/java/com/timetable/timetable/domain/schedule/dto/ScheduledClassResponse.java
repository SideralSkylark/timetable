package com.timetable.timetable.domain.schedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.timetable.timetable.domain.schedule.entity.ScheduledClass;

public record ScheduledClassResponse(
    Long id,
    Long cohortSubjectId, 
    Long timetableId,
    Long roomId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime
) {
    public static ScheduledClassResponse from(ScheduledClass scheduledClass) {
        return new ScheduledClassResponse(
            scheduledClass.getId(), 
            scheduledClass.getCohortSubject().getId(), 
            scheduledClass.getTimetable().getId(), 
            scheduledClass.getRoom().getId(), 
            scheduledClass.getDate(), 
            scheduledClass.getStartTime(), 
            scheduledClass.getEndTime()
        );
    }
}
