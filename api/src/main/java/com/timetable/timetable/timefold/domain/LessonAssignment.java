package com.timetable.timetable.timefold.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import lombok.*;

@Getter 
@Setter
@AllArgsConstructor 
@NoArgsConstructor
@PlanningEntity
public class LessonAssignment {

    @PlanningId
    private Long id;

    // Dados fixos
    private TfSubject subject;
    private TfTeacher teacher;
    private TfCohort cohort;

    // Variáveis a serem calculadas pelo solver
    @PlanningVariable(valueRangeProviderRefs = "rooms")
    private TfRoom room;

    @PlanningVariable(valueRangeProviderRefs = "times")
    private LocalTimeSlot timeSlot;
}

