package com.timetable.timetable.scheduler_engine.solver;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicType;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.SolverManagerConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

@Configuration
public class TimefoldConfig {
    
    @Bean
    public SolverConfig solverConfig() {
        return new SolverConfig()
                .withSolutionClass(com.timetable.timetable.scheduler_engine.domain.TimetableSolution.class)
                .withEntityClasses(com.timetable.timetable.scheduler_engine.domain.LessonAssignment.class)
                .withConstraintProviderClass(TimetableConstraintProvider.class)
                .withTerminationConfig(new TerminationConfig()
                        .withSecondsSpentLimit(30L))
                .withPhases(
                        new ConstructionHeuristicPhaseConfig()
                                .withConstructionHeuristicType(ConstructionHeuristicType.FIRST_FIT),
                        new LocalSearchPhaseConfig()
                                .withTerminationConfig(new TerminationConfig()
                                        .withSecondsSpentLimit(25L))
                );
    }
    
    @Bean
    public SolverFactory<com.timetable.timetable.scheduler_engine.domain.TimetableSolution> solverFactory(SolverConfig solverConfig) {
        return SolverFactory.create(solverConfig);
    }
    
    @Bean
    public SolverManager<com.timetable.timetable.scheduler_engine.domain.TimetableSolution, java.util.UUID> solverManager(
            SolverFactory<com.timetable.timetable.scheduler_engine.domain.TimetableSolution> solverFactory) {
        return SolverManager.create(solverFactory, new SolverManagerConfig());
    }
}
