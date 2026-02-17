package com.timetable.timetable.domain.schedule.dto;

import java.util.List;

import com.timetable.timetable.domain.schedule.entity.Cohort;

public record CohortEstimationResult(
    List<Cohort> cohorts,
    List<String> warnings,
    boolean wasGenerated
) {}
