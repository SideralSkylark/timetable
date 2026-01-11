package com.timetable.timetable.scheduler_engine.solver;

import java.util.UUID;

import com.timetable.timetable.scheduler_engine.domain.TimetableSolution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/solver")
@RequiredArgsConstructor
public class TimetableSolverController {

    private final TimetableSolverService solverService;

    @PostMapping("/start")
    public ResponseEntity<UUID> start(@RequestBody TimetableSolution problem) {
        return ResponseEntity.ok(solverService.startSolverJob(problem));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<TimetableSolution> solution(@PathVariable UUID jobId) {
        TimetableSolution solution = solverService.getSolution(jobId);
        return solution == null
        ? ResponseEntity.accepted().build()
        : ResponseEntity.ok(solution);
    }

}
