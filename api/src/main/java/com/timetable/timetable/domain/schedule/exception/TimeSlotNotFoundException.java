package com.timetable.timetable.domain.schedule.exception;

import com.timetable.timetable.common.exception.ResourceNotFoundException;

public class TimeSlotNotFoundException extends ResourceNotFoundException {
    public TimeSlotNotFoundException(String message) {
        super(message);
    }
}

