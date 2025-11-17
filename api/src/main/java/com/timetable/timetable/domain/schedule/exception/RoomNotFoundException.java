package com.timetable.timetable.domain.schedule.exception;

public class RoomNotFoundException  extends RuntimeException {
    public RoomNotFoundException(String message) {
        super(message);
    }
}
