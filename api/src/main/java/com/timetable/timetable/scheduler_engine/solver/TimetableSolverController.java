package com.timetable.timetable.scheduler_engine.solver;

import java.util.UUID;

import com.timetable.timetable.scheduler_engine.domain.TimetableSolution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ai.timefold.solver.core.api.solver.SolverStatus;

/**
 * REST API for direct solver testing.
 * 
 * This controller allows you to test the solver with raw JSON data
 * WITHOUT touching the database. Useful for:
 * - Quick testing of solver configuration
 * - Debugging constraint logic
 * - Performance benchmarking
 * - Integration tests
 * 
 * Endpoints:
 * - POST /api/v1/solver/test/start - Start solving with JSON input
 * - GET  /api/v1/solver/test/{jobId} - Get solution (poll until ready)
 * - GET  /api/v1/solver/test/{jobId}/status - Get job status
 * - POST /api/v1/solver/test/{jobId}/terminate - Stop solver early
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/solver/test")
@RequiredArgsConstructor
public class TimetableSolverController {
    
    private final TimetableSolverService solverService;
    
    /**
     * Start a solver job with a manually created problem.
     * 
     * Example JSON:
     * {
     *   "lessonAssignments": [
     *     {
     *       "id": 1,
     *       "cohortSubject": { "id": 1, "cohort": {...}, ... },
     *       "blockNumber": 1,
     *       "timeslot": null,
     *       "room": null
     *     }
     *   ],
     *   "availableTimeslots": [ { "id": 1, "dayOfWeek": "MONDAY", ... } ],
     *   "availableRooms": [ { "id": 1, "name": "A101", ... } ],
     *   "academicYear": 2026,
     *   "semester": 1
     * }
     * 
     * @param problem The timetabling problem to solve
     * @return UUID of the solver job (use this to poll for results)
     */
    @PostMapping("/start")
    public ResponseEntity<JobStartedResponse> startSolverJob(@RequestBody TimetableSolution problem) {
        log.info("Received test problem with {} lessons, {} timeslots, {} rooms",
            problem.getTotalLessons(),
            problem.getAvailableTimeslots() != null ? problem.getAvailableTimeslots().size() : 0,
            problem.getAvailableRooms() != null ? problem.getAvailableRooms().size() : 0
        );
        
        UUID jobId = solverService.startSolverJob(problem);
        
        return ResponseEntity.accepted()
            .body(new JobStartedResponse(
                jobId,
                "Solver job started. Poll GET /api/v1/solver/test/" + jobId + " for results.",
                problem.getTotalLessons()
            ));
    }
    
    /**
     * Get the solution for a solver job (if ready).
     * 
     * Returns:
     * - 202 Accepted - Still solving (poll again)
     * - 200 OK - Solution ready (with JSON body)
     * - 404 Not Found - Job ID doesn't exist
     * 
     * @param jobId The job ID from the start request
     * @return The solution (or 202 if still solving)
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<?> getSolution(@PathVariable UUID jobId) {
        TimetableSolution solution = solverService.getSolution(jobId);
        
        if (solution == null) {
            SolverStatus status = solverService.getStatus(jobId);
            
            if (status == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.accepted()
                .body(new JobStatusResponse(jobId, status.toString(), null, null));
        }
        
        log.info("Returning solution for job {}. Score: {}, Feasible: {}", 
            jobId, solution.getScore(), solution.isFeasible());
        
        return ResponseEntity.ok(solution);
    }
    
    /**
     * Get the current status of a solver job.
     * 
     * @param jobId The job ID
     * @return Status information
     */
    @GetMapping("/{jobId}/status")
    public ResponseEntity<JobStatusResponse> getStatus(@PathVariable UUID jobId) {
        SolverStatus status = solverService.getStatus(jobId);
        
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(new JobStatusResponse(
            jobId,
            status.toString(),
            null,
            "Use GET /api/v1/solver/test/" + jobId + " to retrieve the solution when status is NOT_SOLVING"
        ));
    }
    
    /**
     * Terminate a solver job before it finishes naturally.
     * Useful if you realize the problem is too large or want to stop early.
     * 
     * @param jobId The job ID to terminate
     * @return Confirmation message
     */
    @PostMapping("/{jobId}/terminate")
    public ResponseEntity<String> terminateJob(@PathVariable UUID jobId) {
        SolverStatus status = solverService.getStatus(jobId);
        
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (status == SolverStatus.NOT_SOLVING) {
            return ResponseEntity.badRequest()
                .body("Job " + jobId + " is already finished (status: " + status + ")");
        }
        
        solverService.terminateEarly(jobId);
        
        return ResponseEntity.ok("Job " + jobId + " terminated successfully");
    }
    
    // ========================================
    // RESPONSE DTOs
    // ========================================
    
    record JobStartedResponse(
        UUID jobId,
        String message,
        int totalLessonsToSchedule
    ) {}
    
    record JobStatusResponse(
        UUID jobId,
        String status,
        String score,
        String message
    ) {}
}
