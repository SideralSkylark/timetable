package com.timetable.timetable.domain.schedule.entity;

public final class AcademicPolicy {
    public static final int HOURS_PER_CREDIT = 25;
    public static final int LESSON_DURATION_MINUTES = 50;
    public static final int INTERVAL_BETWEEN_LESSONS_MINUTES = 10;
    /** 
     * Use lesson duration due to all timetables sloting each one leason in two contiguous slots including an interval
    */
    public static final int LESSON_DURATION = LESSON_DURATION_MINUTES * 2 + INTERVAL_BETWEEN_LESSONS_MINUTES;
    public static final int WEEKLY_TEACHING_HOURS_LIMIT = 8;
}
