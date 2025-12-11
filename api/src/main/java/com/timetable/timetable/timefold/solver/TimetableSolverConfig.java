package com.timetable.timetable.timefold.solver;

import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimetableSolverConfig {

    @Bean
    public SolverFactory<?> solverFactory() {
        return SolverFactory.create(new SolverConfig()
            .withSolutionClass(com.timetable.timetable.timefold.domain.TimetableSolution.class)
            .withEntityClasses(com.timetable.timetable.timefold.domain.LessonAssignment.class)
            .withConstraintProviderClass(TimetableConstraintProvider.class)
        );
    }
}

