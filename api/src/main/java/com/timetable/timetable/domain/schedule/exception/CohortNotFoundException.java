package com.timetable.timetable.domain.schedule.exception;

public class CohortNotFoundException extends RuntimeException {
    public CohortNotFoundException(String message) {
        super(message);
    }
}
