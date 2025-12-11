package com.timetable.timetable.scheduler_engine.domain;

import ai.timefold.solver.core.api.domain.solution.*;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.*;

import java.util.List;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@PlanningSolution
public class TimetableSolution {

    @ValueRangeProvider(id = "rooms")
    private List<TfRoom> rooms;

    @ValueRangeProvider(id = "times")
    private List<LocalTimeSlot> timeSlots;

    @PlanningEntityCollectionProperty
    private List<LessonAssignment> lessons;

    @PlanningScore
    private HardSoftScore score;
}

