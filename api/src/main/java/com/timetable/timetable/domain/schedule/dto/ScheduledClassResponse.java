package com.timetable.timetable.domain.schedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.timetable.timetable.domain.schedule.entity.ScheduledClass;

public record ScheduledClassResponse(
    Long id,
    SubjectInfo subject,
    Long timetableId,
    TeacherInfo teacher,
    RoomInfo room,
    CohortInfo cohort,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime
) {
    public static ScheduledClassResponse from(ScheduledClass timeSlot) {
        return new ScheduledClassResponse(
            timeSlot.getId(),
            new SubjectInfo(
                timeSlot.getSubject().getId(),
                timeSlot.getSubject().getName()
            ),
            timeSlot.getTimetable() != null ? timeSlot.getTimetable().getId() : null,
            new TeacherInfo(
                timeSlot.getTeacher().getId(),
                timeSlot.getTeacher().getUsername()
            ),
            new RoomInfo(
                timeSlot.getRoom().getId(),
                timeSlot.getRoom().getName(),
                timeSlot.getRoom().getCapacity()
            ),
            new CohortInfo(
                timeSlot.getCohort().getId(),
                timeSlot.getCohort().getDisplayName()
            ),
            timeSlot.getDate(),
            timeSlot.getStartTime(),
            timeSlot.getEndTime()
        );
    }
    
    public record SubjectInfo(
        Long id,
        String name
    ) {}
    
    public record TeacherInfo(
        Long id,
        String name
    ) {}
    
    public record RoomInfo(
        Long id,
        String name,
        Integer capacity
    ) {}
    
    public record CohortInfo(
        Long id,
        String name
    ) {}
}
