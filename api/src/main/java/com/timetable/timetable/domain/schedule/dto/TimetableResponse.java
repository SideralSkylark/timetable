package com.timetable.timetable.domain.schedule.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.timetable.timetable.domain.schedule.entity.Timetable;
import com.timetable.timetable.domain.schedule.entity.TimetableStatus;

public record TimetableResponse(
    Long id,
    String academicPeriod,
    TimetableStatus status,
    LocalDateTime createdAt,
    Integer timeSlotCount,
    List<Long> timeSlotIds
) {
    public static TimetableResponse from(Timetable timetable) {
        List<Long> timeSlotIds = timetable.getTimeSlots() != null 
            ? timetable.getTimeSlots().stream()
                .map(slot -> slot.getId())
                .collect(Collectors.toList())
            : List.of();
            
        return new TimetableResponse(
            timetable.getId(),
            timetable.getAcademicPeriod(),
            timetable.getStatus(),
            timetable.getCreatedAt(),
            timeSlotIds.size(),
            timeSlotIds
        );
    }
}
