package com.timetable.timetable.domain.schedule.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCohortSubjectRequest(
    @NotNull Long assignedTeacherId,
    @NotNull Boolean isActive
) {}
