package com.timetable.timetable.scheduler_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LessonDTO {
    private String time;    // ex: "08:00-09:00"
    private String room;    // ex: "A101"
    private String cohort;  // ex: "A"
    private String teacher; // ex: "Alice"
}
