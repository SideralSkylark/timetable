package com.timetable.timetable.domain.schedule.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateCohortStudentsRequest(
    @NotNull
    List<Long> studentIds
) {}
