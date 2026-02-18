package com.timetable.timetable.domain.schedule.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.timetable.timetable.domain.schedule.entity.Room;

public record RoomResponse(
    Long id,
    String name,
    int capacity,
    Set<RoomRestrictionResponse> restrictions
) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(
            room.getId(),
            room.getName(),
            room.getCapacity(),
            room.getRestrictions()
                .stream()
                .map(RoomRestrictionResponse::from)
                .collect(Collectors.toSet())
        );
    }
}
