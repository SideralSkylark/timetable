package com.timetable.timetable.domain.schedule.entity;

/**
 * Centralized academic policies.
 *
 * Scheduling is based on institutional contact policy:
 * every discipline meets 2x per week regardless of credits.
 */
public final class AcademicPolicy {

    /** Every discipline meets 2 sessions per week. */
    public static final int SESSIONS_PER_WEEK = 2;

    /** Maximum weekly teaching hours per teacher (3 disciplines max). */
    public static final int WEEKLY_TEACHING_HOURS_LIMIT = 12;

    /** Weekly contact hours per discipline for teacher workload calculation. */
    public static final int WEEKLY_CONTACT_HOURS = 4;

    public static final int ESTIMATED_STUDENT_COUNT = 35;

    public static int calculateLessonBlocksPerWeek(int credits) {
        return SESSIONS_PER_WEEK;
    }

    public static int calculateWeeklyHours(int credits) {
        return WEEKLY_CONTACT_HOURS;
    }

    public static boolean areCreditsValid(int credits) {
        return credits >= 3 && credits <= 12;
    }

    private AcademicPolicy() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}
