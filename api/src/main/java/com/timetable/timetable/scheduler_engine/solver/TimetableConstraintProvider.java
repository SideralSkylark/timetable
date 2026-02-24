package com.timetable.timetable.scheduler_engine.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.*;

import com.timetable.timetable.domain.schedule.entity.TimePeriod;
import com.timetable.timetable.scheduler_engine.domain.LessonAssignment;

/**
 * Defines the constraints for the timetabling problem.
 * Hard constraints must be satisfied for a feasible solution.
 * Soft constraints are preferences that should be optimized.
 */
public class TimetableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                // HARD CONSTRAINTS (must be satisfied)
                teacherConflict(factory),
                roomConflict(factory),
                cohortConflict(factory),
                roomCapacity(factory),
                roomCourseRestriction(factory),
                YearPeriodRestriction(factory),
                sameSubjectConsecutiveSameDay(factory),

                // SOFT CONSTRAINTS (preferences to optimize)
                // minimizeTeacherGaps(factory),
                // distributeSubjectEvenly(factory)
        };
    }

    // ========================================
    // HARD CONSTRAINTS
    // ========================================

    /**
     * HC1: A teacher cannot teach two lessons at the same time.
     */
    private Constraint teacherConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(
                LessonAssignment.class,
                Joiners.equal(LessonAssignment::getTeacher),
                Joiners.equal(LessonAssignment::getTimeslot))
                .filter((lesson1, lesson2) -> lesson1.getTimeslot() != null &&
                        lesson1.getTeacher() != null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher conflict");
    }

    /**
     * HC2: A room cannot host two lessons at the same time.
     */
    private Constraint roomConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(
                LessonAssignment.class,
                Joiners.equal(LessonAssignment::getRoom),
                Joiners.equal(LessonAssignment::getTimeslot))
                .filter((lesson1, lesson2) -> lesson1.getRoom() != null &&
                        lesson1.getTimeslot() != null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Room conflict");
    }

    /**
     * HC3: A cohort (student group) cannot have two lessons at the same time.
     */
    private Constraint cohortConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(
                LessonAssignment.class,
                Joiners.equal(LessonAssignment::getCohort),
                Joiners.equal(LessonAssignment::getTimeslot))
                .filter((lesson1, lesson2) -> lesson1.getTimeslot() != null &&
                        lesson1.getCohort() != null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Cohort conflict");
    }

    /**
     * HC4: A room must have sufficient capacity for the cohort's students.
     */
    private Constraint roomCapacity(ConstraintFactory factory) {
        return factory.forEach(LessonAssignment.class)
                .filter(lesson -> lesson.getRoom() != null &&
                        lesson.getCohort() != null &&
                        !lesson.getRoom().hasSufficientCapacity(lesson.getStudentCount()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Insufficient room capacity");
    }

    /**
     * HC5: If a room is restricted to a specific course, only that course can use
     * it.
     */
    private Constraint roomCourseRestriction(ConstraintFactory factory) {
        return factory.forEach(LessonAssignment.class)
                .filter(lesson -> lesson.getRoom() != null &&
                        lesson.getCourseId() != null &&
                        !lesson.getRoom().isAvailableForCourse(
                                lesson.getCourseId(),
                                lesson.getTimeslot().getPeriod()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Room course restriction violated");
    }

    /**
     * HC6: If a lesson is to be taught in the morning or evening
     */
    private Constraint YearPeriodRestriction(ConstraintFactory factory) {
        return factory.forEach(LessonAssignment.class)
                .filter(lesson -> lesson.getTimeslot() != null &&
                        lesson.getCohort() != null &&
                        !isPeriodAllowedForYear(
                                lesson.getCohort().getYear(),
                                lesson.getTimeslot().getPeriod()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("year-period restriction violation");
    }

    /**
     * HC7:
     * Prevent two consecutive lessons of the same subject
     * for the same cohort on the same day.
     */
    private Constraint sameSubjectConsecutiveSameDay(ConstraintFactory factory) {
        return factory.forEachUniquePair(
                LessonAssignment.class,
                Joiners.equal(LessonAssignment::getCohort),
                Joiners.equal(LessonAssignment::getSubject))
                .filter((l1, l2) -> l1.getTimeslot() != null &&
                        l2.getTimeslot() != null &&
                        isSameDay(l1, l2) &&
                        areConsecutive(l1, l2))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Same subject consecutive lessons same day");
    }

    // ========================================
    // SOFT CONSTRAINTS (commented out for now)
    // ========================================

    /**
     * SC1: Minimize gaps in a teacher's schedule (same day).
     * Teachers prefer to have consecutive lessons without breaks.
     */
    /*
     * private Constraint minimizeTeacherGaps(ConstraintFactory factory) {
     * return factory.forEach(LessonAssignment.class)
     * .join(LessonAssignment.class,
     * Joiners.equal(LessonAssignment::getTeacher),
     * Joiners.equal(lesson -> lesson.getTimeslot().getDayOfWeek())
     * )
     * .filter((lesson1, lesson2) ->
     * lesson1.getTimeslot() != null &&
     * lesson2.getTimeslot() != null &&
     * hasGapBetween(lesson1.getTimeslot(), lesson2.getTimeslot())
     * )
     * .penalize(HardSoftScore.ONE_SOFT)
     * .asConstraint("Teacher schedule gap");
     * }
     */

    /**
     * SC2: Distribute lessons of the same subject evenly across the week.
     */
    /*
     * private Constraint distributeSubjectEvenly(ConstraintFactory factory) {
     * return factory.forEach(LessonAssignment.class)
     * .join(LessonAssignment.class,
     * Joiners.equal(lesson -> lesson.getCohortSubject().getId()),
     * Joiners.equal(lesson -> lesson.getTimeslot().getDayOfWeek())
     * )
     * .filter((lesson1, lesson2) ->
     * lesson1.getId() < lesson2.getId() // Avoid double counting
     * )
     * .penalize(HardSoftScore.ONE_SOFT)
     * .asConstraint("Subject lessons on same day");
     * }
     */

    private boolean isPeriodAllowedForYear(int year, TimePeriod period) {
        boolean isOddYear = year % 2 != 0;

        if (isOddYear) {
            return period == TimePeriod.MORNING;
        } else {
            return period == TimePeriod.AFTERNOON;
        }
    }

    private boolean isSameDay(LessonAssignment l1, LessonAssignment l2) {
        return l1.getTimeslot().getDayOfWeek()
                .equals(l2.getTimeslot().getDayOfWeek());
    }

    private boolean areConsecutive(LessonAssignment l1, LessonAssignment l2) {
        return l1.getTimeslot().getEndTime()
                .equals(l2.getTimeslot().getStartTime())
                || l2.getTimeslot().getEndTime()
                        .equals(l1.getTimeslot().getStartTime());
    }
}
