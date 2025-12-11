package com.timetable.timetable.scheduler_engine.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.*;
import com.timetable.timetable.scheduler_engine.domain.*;

public class TimetableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
            teacherConflict(factory),
            roomConflict(factory),
            cohortConflict(factory),
            roomCapacity(factory)
        };
    }

    // 1. Professor não pode dar 2 aulas no mesmo horário
    private Constraint teacherConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(LessonAssignment.class,
                Joiners.equal(a -> a.getTeacher().getId()),
                Joiners.equal(a -> a.getTimeSlot()))
            .penalize("Teacher conflict", HardSoftScore.ONE_HARD);
    }

    // 2. Duas aulas não podem ocorrer na mesma sala no mesmo horário
    private Constraint roomConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(LessonAssignment.class,
                Joiners.equal(a -> a.getRoom().getId()),
                Joiners.equal(a -> a.getTimeSlot()))
            .penalize("Room conflict", HardSoftScore.ONE_HARD);
    }

    // 3. Uma turma não pode ter duas aulas no mesmo horário
    private Constraint cohortConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(LessonAssignment.class,
                Joiners.equal(a -> a.getCohort().getId()),
                Joiners.equal(a -> a.getTimeSlot()))
            .penalize("Cohort conflict", HardSoftScore.ONE_HARD);
    }

    // 4. Sala precisa ter capacidade suficiente
    private Constraint roomCapacity(ConstraintFactory factory) {
        return factory.forEach(LessonAssignment.class)
            .filter(a ->
                a.getRoom() != null &&
                a.getCohort() != null &&
                a.getRoom().getCapacity() < 30 // Podes substituir pelo tamanho real da turma
            )
            .penalize("Insufficient room capacity", HardSoftScore.ONE_HARD);
    }
}

