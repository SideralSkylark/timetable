package com.timetable.timetable.timefold.mapper;

import com.timetable.timetable.domain.schedule.entity.*;
import com.timetable.timetable.timefold.domain.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TimetableSolverMapper {

    public TimetableSolution toSolution(Timetable timetable,
                                       List<TfRoom> rooms,
                                       List<LocalTimeSlot> possibleTimeSlots) {

        List<LessonAssignment> lessons = timetable.getTimeSlots().stream()
            .map(this::mapLesson)
            .toList();

        return new TimetableSolution(
            rooms,
            possibleTimeSlots,
            lessons,
            null
        );
    }

    private LessonAssignment mapLesson(TimeSlot ts) {
        return new LessonAssignment(
            ts.getId(),
            new TfSubject(ts.getSubject().getId(), ts.getSubject().getName()),
            new TfTeacher(ts.getTeacher().getId(), ts.getTeacher().getUsername()),
            new TfCohort(ts.getCohort().getId(), ts.getCohort().getName()),
            null,
            null
        );
    }
}

