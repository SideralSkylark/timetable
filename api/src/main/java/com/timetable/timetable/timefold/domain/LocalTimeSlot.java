package com.timetable.timetable.timefold.domain;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class LocalTimeSlot {
    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
}

