package com.timetable.timetable.domain.schedule.entity;

/**
 * Centralized academic policies
 */
public final class AcademicPolicy {
    
    // Constantes base
    public static final int HOURS_PER_CREDIT = 25;
    public static final int WEEKS_PER_SEMESTER = 15;
    public static final int LESSON_DURATION_MINUTES = 50;
    public static final int INTERVAL_BETWEEN_LESSONS_MINUTES = 10;
    public static final int WEEKLY_TEACHING_HOURS_LIMIT = 8;
    
    /**
     * Total duration of a lesson block (2 slots + 1 break)
     */
    public static final int LESSON_BLOCK_DURATION_MINUTES = 
        (LESSON_DURATION_MINUTES * 2) + INTERVAL_BETWEEN_LESSONS_MINUTES;
    
    /**
     * Total hours for a lesson given n credits
     */
    public static int getTotalHours(int credits) {
        return HOURS_PER_CREDIT * credits;
    }
    
    /**
     * Hours per week for a lesson
     */
    public static int calculateWeeklyHours(int credits) {
        int totalHours = getTotalHours(credits);
        return totalHours / WEEKS_PER_SEMESTER;
    }
    
    /**
     * Total lesson blocks per week
     * ie: lesson has 6 credits, then lesson = 10 hours/week = ~5 blocks of 110min
     */
    public static int calculateLessonBlocksPerWeek(int credits) {
        int weeklyMinutes = calculateWeeklyHours(credits) * 60;
        return (int) Math.ceil((double) weeklyMinutes / LESSON_BLOCK_DURATION_MINUTES);
    }
    
    /**
     * Calculates 50 minute slots per week
     */
    public static int calculateSlotsPerWeek(int credits) {
        int weeklyMinutes = calculateWeeklyHours(credits) * 60;
        return weeklyMinutes / LESSON_DURATION_MINUTES;
    }
    
    /**
     * Validades a lesson's credits (usually 3 <= credits <= 12)
     */
    public static boolean areCreditsValid(int credits) {
        return credits >= 3 && credits <= 12;
    }
    
    /**
     * Total blocks of lessons in a semester
     */
    public static int getTotalLessonBlocksInSemester(int credits) {
        return calculateLessonBlocksPerWeek(credits) * WEEKS_PER_SEMESTER;
    }
    
    private AcademicPolicy() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}
