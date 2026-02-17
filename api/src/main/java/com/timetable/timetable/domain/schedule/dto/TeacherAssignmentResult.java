package com.timetable.timetable.domain.schedule.dto;

import com.timetable.timetable.domain.user.entity.ApplicationUser;

public record TeacherAssignmentResult(
    ApplicationUser teacher,
    boolean isPhantom,
    String warning
) {}
