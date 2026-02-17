package com.timetable.timetable.domain.schedule.dto;

import java.util.List;

public record PreSolverRequest(
    int academicYear,
    int semester,
    List<Long> courseIds,            // null = todos
    CohortEstimationConfig estimationConfig,  // null = default
    PhantomTeacherPolicy policy      // default: CREATE_IF_NEEDED
) {
    // Construtor conveniente com defaults
    static PreSolverRequest withDefaults(int academicYear, int semester) {
        return new PreSolverRequest(
            academicYear, semester, null,
            CohortEstimationConfig.defaultConfig(),
            PhantomTeacherPolicy.CREATE_IF_NEEDED
        );
    }
}
