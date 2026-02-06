package com.timetable.timetable.domain.schedule.dto;

import java.util.Set;

import com.timetable.timetable.domain.schedule.entity.Room;
import com.timetable.timetable.domain.schedule.entity.RoomCourseRestriction;

public record RoomResponse(
    Long id,
    String name,
    int capacity,
    Set<RoomCourseRestriction> restricoes
) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(
            room.getId(),
            room.getName(),
            room.getCapacity(),
            room.getRestrictions()
        );
    }
}
