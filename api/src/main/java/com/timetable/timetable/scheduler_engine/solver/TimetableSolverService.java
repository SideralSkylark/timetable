package com.timetable.timetable.scheduler_engine.solver;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.timetable.timetable.scheduler_engine.domain.TimetableSolution;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimetableSolverService {
    
    private final SolverManager<TimetableSolution, UUID> solverManager;
    private final Map<UUID, SolverJob<TimetableSolution, UUID>> jobs = new ConcurrentHashMap<>();
    
    public UUID startSolverJob(TimetableSolution problem) {
        UUID jobId = UUID.randomUUID();
        log.info("Starting solver job with ID: {}", jobId);
        SolverJob<TimetableSolution, UUID> job = solverManager.solve(jobId, problem);
        jobs.put(jobId, job);
        return jobId;
    }
    
    public TimetableSolution getSolution(UUID jobId) {
        SolverJob<TimetableSolution, UUID> job = jobs.get(jobId);
        if (job == null) {
            log.warn("Job {} not found", jobId);
            return null;
        }
        
        SolverStatus status = job.getSolverStatus();
        log.info("Solver status for job {}: {}", jobId, status);
        
        if (status == SolverStatus.NOT_SOLVING) {
            try {
                TimetableSolution solution = job.getFinalBestSolution();
                log.info("Solution retrieved for job {}. Score: {}", jobId, solution.getScore());
                jobs.remove(jobId);
                return solution;
            } catch (InterruptedException e) {
                log.error("Thread interrupted while retrieving solution for job {}", jobId, e);
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error("Error retrieving solution for job {}", jobId, e);
            }
        }
        
        return null;
    }
    
    // Método para aguardar a solução de forma síncrona (útil para testes)
    public TimetableSolution getSolutionAndWait(UUID jobId, long timeout, TimeUnit unit) {
        SolverJob<TimetableSolution, UUID> job = jobs.get(jobId);
        if (job == null) {
            log.warn("Job {} not found", jobId);
            return null;
        }
        
        try {
            TimetableSolution solution = job.getFinalBestSolution();
            log.info("Solution retrieved for job {}. Score: {}", jobId, solution.getScore());
            jobs.remove(jobId);
            return solution;
        } catch (InterruptedException e) {
            log.error("Thread interrupted while waiting for solution for job {}", jobId, e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("Error retrieving solution for job {}", jobId, e);
        }
        
        return null;
    }
    
    public SolverStatus getStatus(UUID jobId) {
        SolverJob<TimetableSolution, UUID> job = jobs.get(jobId);
        return job != null ? job.getSolverStatus() : null;
    }
    
    public void terminateEarly(UUID jobId) {
        log.info("Terminating solver job {} early", jobId);
        solverManager.terminateEarly(jobId);
        jobs.remove(jobId);
    }
}
