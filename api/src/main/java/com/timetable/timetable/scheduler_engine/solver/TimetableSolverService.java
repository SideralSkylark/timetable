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
import com.timetable.timetable.domain.schedule.entity.ScheduledClass;
import com.timetable.timetable.domain.schedule.entity.Timeslot;
import com.timetable.timetable.domain.schedule.entity.Timetable;
import com.timetable.timetable.domain.schedule.entity.TimetableStatus;
import com.timetable.timetable.domain.schedule.repository.CohortSubjectRepository;
import com.timetable.timetable.domain.schedule.repository.RoomRepository;
import com.timetable.timetable.domain.schedule.repository.ScheduledClassRepository;
import com.timetable.timetable.domain.schedule.repository.TimeslotRepository;
import com.timetable.timetable.domain.schedule.repository.TimetableRepository;
import com.timetable.timetable.scheduler_engine.domain.TimetableSolution;
import com.timetable.timetable.scheduler_engine.mapper.PersistenceMapper;
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
    private final TimetableRepository timetableRepository;
    private final ScheduledClassRepository scheduledClassRepository;
    private final TimetableSolutionMapper solutionMapper;
    private final PersistenceMapper persistenceMapper;
    
    /**
     * starts a solver job
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
    public Timetable generateTimetable(int academicYear, int semester) {
        return generateTimetable(academicYear, semester, 60L);
    }

    /**
     * Generates a complete timetable for the specified academic period. Assuming existing cohorts have been delegated
     * 
     * @param academicYear The academic year (e.g., 2026)
     * @param semester The semester (1 or 2)
     * @param solverTimeoutSeconds Maximum time to spend solving (default: 60s)
     * @return The generated Timetable with all scheduled classes
     * @throws IllegalStateException if no feasible solution is found
     */
    @Transactional
    public Timetable generateTimetable(int academicYear, int semester, long solverTimeoutSeconds) {
        
        log.info("=".repeat(80));
        log.info("Starting timetable generation for {}.{}", academicYear, semester);
        log.info("=".repeat(80));
        
        log.info("Step 1/5: Fetching data from database...");
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
        
        log.info("Step 2/5: Converting to solver domain...");
        
        TimetableSolution problem = solutionMapper.toPlanningProblem(
            cohortSubjects, timeslots, rooms, academicYear, semester
        );
        
        log.info("Created planning problem with {} lesson assignments",
            problem.getTotalLessons());
        
        log.info("Step 3/5: Starting Timefold solver (timeout: {}s)...", solverTimeoutSeconds);
        
        UUID jobId = startSolverJob(problem);
        log.info("Solver job started with ID: {}", jobId);
        
        TimetableSolution solution = getSolutionAndWait(
            jobId, solverTimeoutSeconds, TimeUnit.SECONDS
        );
        
        if (solution == null) {
            throw new IllegalStateException(
                "Solver failed to produce a solution within " + solverTimeoutSeconds + " seconds");
        }
        
        log.info("Solver finished! {}", persistenceMapper.getSolutionStats(solution));
        
        log.info("Step 4/5: Validating solution...");
        
        if (!solution.isFeasible()) {
            log.error("Solution is NOT feasible! Hard constraints violated.");
            log.error("Score: {}", solution.getScore());
            throw new IllegalStateException(
                "Solver could not find a feasible solution. Score: " + solution.getScore() +
                ". Try: (1) Add more timeslots, (2) Add more rooms, (3) Reduce cohort-subjects, " +
                "(4) Increase solver timeout"
            );
        }
        
        if (!persistenceMapper.isSolutionComplete(solution)) {
            log.warn("Solution is feasible but incomplete (some lessons unassigned)");
        }
        
        log.info("Solution is FEASIBLE. Score: {}", solution.getScore());
        
        log.info("Step 5/5: Persisting solution to database...");
        
        // Create Timetable entity
        Timetable timetable = Timetable.builder()
            .academicYear(academicYear)
            .semester(semester)
            .status(TimetableStatus.DRAFT)
            .build();
        
        timetableRepository.save(timetable);
        log.info("Created Timetable #{} for {}.{}", 
            timetable.getId(), academicYear, semester);
        
        // Convert solution to ScheduledClass entities
        List<ScheduledClass> scheduledClasses = persistenceMapper.convertToScheduledClasses(
            solution, timetable
        );
        
        // Save all scheduled classes
        scheduledClassRepository.saveAll(scheduledClasses);
        timetable.setScheduledClasses(scheduledClasses);
        timetableRepository.save(timetable);
        
        log.info("Successfully saved {} scheduled classes", scheduledClasses.size());
        
        log.info("=".repeat(80));
        log.info("Timetable generation COMPLETE!");
        log.info("Timetable ID: {}", timetable.getId());
        log.info("Period: {}.{}", academicYear, semester);
        log.info("Lessons scheduled: {}", scheduledClasses.size());
        log.info("Score: {} (0 hard = feasible)", solution.getScore());
        log.info("=".repeat(80));
        
        return timetable;
    }
}
