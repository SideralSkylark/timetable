package com.timetable.timetable.scheduler_engine.solver;

import com.timetable.timetable.domain.schedule.entity.*;
import com.timetable.timetable.domain.schedule.repository.*;
import com.timetable.timetable.scheduler_engine.domain.TimetableSolution;
import com.timetable.timetable.scheduler_engine.mapper.PersistenceMapper;
import com.timetable.timetable.scheduler_engine.mapper.TimetableSolutionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * High-level service for generating timetables.
 * 
 * This orchestrates the entire flow:
 * 1. Fetch data from database
 * 2. Convert to solver domain
 * 3. Run the solver
 * 4. Convert solution back to JPA entities
 * 5. Persist the results
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TimetableGenerationService {
    
    // Repositories
    private final CohortSubjectRepository cohortSubjectRepository;
    private final TimeslotRepository timeslotRepository;
    private final RoomRepository roomRepository;
    private final TimetableRepository timetableRepository;
    private final ScheduledClassRepository scheduledClassRepository;
    
    // Mappers
    private final TimetableSolutionMapper solutionMapper;
    private final PersistenceMapper persistenceMapper;
    
    // Solver
    private final TimetableSolverService solverService;
    
    /**
     * Generates a complete timetable for the specified academic period.
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
        
        // ========================================
        // STEP 1: FETCH DATA FROM DATABASE
        // ========================================
        
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
        
        // ========================================
        // STEP 2: CONVERT TO SOLVER DOMAIN
        // ========================================
        
        log.info("Step 2/5: Converting to solver domain...");
        
        TimetableSolution problem = solutionMapper.toPlanningProblem(
            cohortSubjects, timeslots, rooms, academicYear, semester
        );
        
        log.info("Created planning problem with {} lesson assignments",
            problem.getTotalLessons());
        
        // ========================================
        // STEP 3: RUN THE SOLVER
        // ========================================
        
        log.info("Step 3/5: Starting Timefold solver (timeout: {}s)...", solverTimeoutSeconds);
        
        UUID jobId = solverService.startSolverJob(problem);
        log.info("Solver job started with ID: {}", jobId);
        
        TimetableSolution solution = solverService.getSolutionAndWait(
            jobId, solverTimeoutSeconds, TimeUnit.SECONDS
        );
        
        if (solution == null) {
            throw new IllegalStateException(
                "Solver failed to produce a solution within " + solverTimeoutSeconds + " seconds");
        }
        
        log.info("Solver finished! {}", persistenceMapper.getSolutionStats(solution));
        
        // ========================================
        // STEP 4: VALIDATE SOLUTION
        // ========================================
        
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
        
        // ========================================
        // STEP 5: PERSIST SOLUTION
        // ========================================
        
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
        
        // ========================================
        // DONE!
        // ========================================
        
        log.info("=".repeat(80));
        log.info("Timetable generation COMPLETE!");
        log.info("Timetable ID: {}", timetable.getId());
        log.info("Period: {}.{}", academicYear, semester);
        log.info("Lessons scheduled: {}", scheduledClasses.size());
        log.info("Score: {} (0 hard = feasible)", solution.getScore());
        log.info("=".repeat(80));
        
        return timetable;
    }
    
    /**
     * Convenience method with default 60-second timeout.
     */
    @Transactional
    public Timetable generateTimetable(int academicYear, int semester) {
        return generateTimetable(academicYear, semester, 60L);
    }
    
    /**
     * Regenerates a timetable (deletes old scheduled classes and creates new ones).
     */
    @Transactional
    public Timetable regenerateTimetable(Long timetableId, long solverTimeoutSeconds) {
        Timetable existingTimetable = timetableRepository.findById(timetableId)
            .orElseThrow(() -> new IllegalArgumentException("Timetable not found: " + timetableId));
        
        log.info("Regenerating timetable #{} for {}.{}", 
            timetableId, existingTimetable.getAcademicYear(), existingTimetable.getSemester());
        
        // Delete old scheduled classes
        scheduledClassRepository.deleteByTimetableId(timetableId);
        log.info("Deleted old scheduled classes");
        
        // Generate new timetable (but delete the old entity and create fresh)
        timetableRepository.delete(existingTimetable);
        
        return generateTimetable(
            existingTimetable.getAcademicYear(),
            existingTimetable.getSemester(),
            solverTimeoutSeconds
        );
    }
}
