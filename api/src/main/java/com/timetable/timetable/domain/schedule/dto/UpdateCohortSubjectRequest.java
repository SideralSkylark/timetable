package com.timetable.timetable.domain.schedule.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCohortSubjectRequest(
    @NotNull Long assignedTeacherId,
    @NotNull Integer weeklyHours,
    @NotNull Integer lessonsPerWeek,
    @NotNull Boolean isActive
) {}
