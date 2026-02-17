package com.timetable.timetable.domain.schedule.dto;

public record CohortEstimationConfig(
    int courseYears,
    int cohortsYear1,
    int cohortsYear2,
    int cohortsYear3Plus,
    int estimatedStudentsPerCohort
) {
    // Config padrão do orientador: 3 turmas no 1º ano, 2 no resto
    public static CohortEstimationConfig defaultConfig() {
        return new CohortEstimationConfig(3, 3, 2, 2, 35);
    }
    
    // Config personalizada por curso
    static CohortEstimationConfig forCourse(int years, int cohortsYear1) {
        return new CohortEstimationConfig(years, cohortsYear1, 2, 2, 35);
    }
    
    public int cohortsForYear(int year) {
        return switch (year) {
            case 1 -> cohortsYear1;
            case 2 -> cohortsYear2;
            default -> cohortsYear3Plus;
        };
    }
}
