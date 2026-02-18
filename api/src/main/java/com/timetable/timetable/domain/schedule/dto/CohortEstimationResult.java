package com.timetable.timetable.domain.schedule.dto;

import java.util.List;

import com.timetable.timetable.domain.schedule.entity.Cohort;

/**
 * response object for estimated cohort
 */
public record CohortEstimationResult(
    List<Cohort> cohorts,
    List<String> warnings,
    boolean wasGenerated
) {}
