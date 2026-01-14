package com.timetable.timetable.domain.schedule.entity;

public final class AcademicPolicy {
    public static final int HOURS_PER_CREDIT = 25;
    public static final int LESSON_DURATION_MINUTES = 50;
    public static final int INTERVAL_BETWEEN_LESSONS_MINUTES = 10;
    public static final int WEEKS_PER_SEMESTER = 15;
    /** 
     * Use lesson duration due to all timetables sloting each one leason in two contiguous slots including an interval
    */
    public static final int LESSON_DURATION = LESSON_DURATION_MINUTES * 2 + INTERVAL_BETWEEN_LESSONS_MINUTES;
    public static final int WEEKLY_TEACHING_HOURS_LIMIT = 8;

    public int getTotalHours(int credits) {
        return AcademicPolicy.HOURS_PER_CREDIT * credits;
    }

    public int calculateWeeklyHours(int credits) {
        int totalHours = credits * HOURS_PER_CREDIT;
        return totalHours / WEEKS_PER_SEMESTER;
    }

    public int calculateLessonsPerWeek(int credits) {
        int weeklyHours = calculateWeeklyHours(credits);
        return weeklyHours / LESSON_DURATION;
    }

}
