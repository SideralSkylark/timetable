package com.timetable.timetable.domain.schedule.dto;

import com.timetable.timetable.domain.schedule.entity.Room;

public record RoomResponse(
    Long id,
    String name,
    int capacity
) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(
            room.getId(),
            room.getName(),
            room.getCapacity()
        );
    }
}
