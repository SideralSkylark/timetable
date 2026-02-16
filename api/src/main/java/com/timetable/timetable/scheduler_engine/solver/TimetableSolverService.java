package com.timetable.timetable.scheduler_engine.solver;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.timetable.timetable.domain.schedule.entity.CohortSubject;
import com.timetable.timetable.domain.schedule.entity.Room;
import com.timetable.timetable.domain.schedule.entity.Timeslot;
import com.timetable.timetable.domain.schedule.repository.CohortSubjectRepository;
import com.timetable.timetable.domain.schedule.repository.RoomRepository;
import com.timetable.timetable.domain.schedule.repository.TimeslotRepository;
import com.timetable.timetable.scheduler_engine.domain.TimetableSolution;
import com.timetable.timetable.scheduler_engine.mapper.TimetableSolutionMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimetableSolverService {
    
    private final SolverManager<TimetableSolution, UUID> solverManager;
    private final Map<UUID, SolverJob<TimetableSolution, UUID>> jobs = new ConcurrentHashMap<>();

    private final CohortSubjectRepository cohortSubjectRepository;
    private final RoomRepository roomRepository;
    private final TimeslotRepository timeslotRepository;
    private final TimetableSolutionMapper solutionMapper;
    
    /**
     * starts a solver job from a TimetableSolution. Used for tests
     * @param problem
     * @return job UUID 
     */
    public UUID startSolverJob(TimetableSolution problem) {
        UUID jobId = UUID.randomUUID();
        log.info("Starting solver job (no callback) with ID: {}", jobId);
        SolverJob<TimetableSolution, UUID> job = solverManager.solve(jobId, problem);
        jobs.put(jobId, job);
        return jobId;
    }
    
    /**
     * gets solution by its UUID
     */
    public TimetableSolution getSolution(UUID jobId) {
        SolverJob<TimetableSolution, UUID> job = jobs.get(jobId);
        if (job == null) {
            log.warn("Job {} not found", jobId);
            return null;
        }
        
        SolverStatus status = job.getSolverStatus();
        log.debug("Solver status for job {}: {}", jobId, status);
        
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
    
    /**
     * waits for a solution (sync)
     */
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
    
    /**
     * check job status
     */
    public SolverStatus getStatus(UUID jobId) {
        SolverJob<TimetableSolution, UUID> job = jobs.get(jobId);
        return job != null ? job.getSolverStatus() : null;
    }

    /**
     * terminates a running job
     */
    public void terminateEarly(UUID jobId) {
        log.info("Terminating solver job {} early", jobId);
        solverManager.terminateEarly(jobId);
        jobs.remove(jobId);
    }

    /**
     * Convenience method with default 60-second timeout.
     */
    @Transactional
    public UUID generateTimetable(int academicYear, int semester) {
        return generateTimetable(academicYear, semester, 60L);
    }

    /**
     * Starts a job to generate a timetable for the specified academic period. Assuming existing cohorts have been delegated
     * 
     * @param academicYear The academic year (e.g., 2026)
     * @param semester The semester (1 or 2)
     * @param solverTimeoutSeconds Maximum time to spend solving (default: 60s)
     * @return The jobId for the timetable
     * @throws IllegalStateException if no feasible solution is found
     */
    @Transactional
    public UUID generateTimetable(int academicYear, int semester, long solverTimeoutSeconds) {
        
        log.info("=".repeat(80));
        log.info("Starting timetable generation for {}.{}", academicYear, semester);
        log.info("=".repeat(80));
        
        log.info("Step 1/3: Fetching data from database...");
        List<CohortSubject> cohortSubjects = cohortSubjectRepository
            .findByAcademicYearAndSemesterAndIsActive(academicYear, semester, true);
        
        List<Timeslot> timeslots = timeslotRepository.findAll();
        List<Room> rooms = roomRepository.findAll();
        
        log.info("Found {} active cohort-subjects, {} timeslots, {} rooms",
            cohortSubjects.size(), timeslots.size(), rooms.size());
        // Validate inputs
        if (cohortSubjects.isEmpty()) {
            throw new IllegalStateException(
                "No active cohort-subjects found for " + academicYear + "." + semester);
        }
        if (timeslots.isEmpty()) {
            throw new IllegalStateException("No timeslots available for scheduling");
        }
        if (rooms.isEmpty()) {
            throw new IllegalStateException("No rooms available for scheduling");
        }
        
        log.info("Step 2/3: Converting to solver domain...");
        
        TimetableSolution problem = solutionMapper.toPlanningProblem(
            cohortSubjects, timeslots, rooms, academicYear, semester
        );
        
        log.info("Created planning problem with {} lesson assignments",
            problem.getTotalLessons());
        
        log.info("Step 3/3: Starting Timefold solver (timeout: {}s)...", solverTimeoutSeconds);
        
        UUID jobId = startSolverJob(problem);
        log.info("Solver job started with ID: {}", jobId);

        SolverJob<TimetableSolution, UUID> job = solverManager.solve(jobId, problem);
        jobs.put(jobId, job);

        return jobId;
    }
}
