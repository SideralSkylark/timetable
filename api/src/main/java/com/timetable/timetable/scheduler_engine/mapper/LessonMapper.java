package com.timetable.timetable.scheduler_engine.mapper;

import com.timetable.timetable.scheduler_engine.domain.LessonAssignment;
import com.timetable.timetable.scheduler_engine.dto.LessonDTO;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class LessonMapper {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public LessonDTO toDto(LessonAssignment lesson) {
        String time = lesson.getTimeSlot() != null
                ? lesson.getTimeSlot().getStart().format(TIME_FORMAT) +
                  "-" +
                  lesson.getTimeSlot().getEnd().format(TIME_FORMAT)
                : "UNASSIGNED";

        String room = lesson.getRoom() != null ? lesson.getRoom().getName() : "UNASSIGNED";
        String cohort = lesson.getCohort() != null ? lesson.getCohort().getName() : "UNASSIGNED";
        String teacher = lesson.getTeacher() != null ? lesson.getTeacher().getName() : "UNASSIGNED";

        return new LessonDTO(time, room, cohort, teacher);
    }
}
