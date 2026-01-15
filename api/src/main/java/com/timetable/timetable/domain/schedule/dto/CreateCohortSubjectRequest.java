package com.timetable.timetable.domain.schedule.dto;

import jakarta.validation.constraints.NotNull;

public record CreateCohortSubjectRequest(
    @NotNull Long cohortId,
    @NotNull Long subjectId,
    @NotNull Long assignedTeacherId,
    @NotNull Integer weeklyHours,
    @NotNull Integer lessonsPerWeek
) {}

