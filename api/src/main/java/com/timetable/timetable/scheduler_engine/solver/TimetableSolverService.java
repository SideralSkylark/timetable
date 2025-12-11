package com.timetable.timetable.scheduler_engine.solver;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import com.timetable.timetable.scheduler_engine.domain.TimetableSolution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimetableSolverService {

    private final SolverFactory<TimetableSolution> solverFactory;

    public TimetableSolution solve(TimetableSolution problem) {
        Solver<TimetableSolution> solver = solverFactory.buildSolver();
        return solver.solve(problem);
    }
}

