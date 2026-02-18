package com.timetable.timetable.scheduler_engine.preparation;

import com.timetable.timetable.domain.schedule.dto.CohortEstimationConfig;
import com.timetable.timetable.domain.schedule.dto.CohortEstimationResult;
import com.timetable.timetable.domain.schedule.dto.PreSolverRequest;
import com.timetable.timetable.domain.schedule.dto.PreSolverResult;
import com.timetable.timetable.domain.schedule.dto.TeacherAssignmentResult;
import com.timetable.timetable.domain.schedule.entity.*;
import com.timetable.timetable.domain.schedule.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Orquestrates the data before the solver can ran.
 * Garantees that all {@link CohortSubject} exist with teachers assigned to them.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PreSolverService {
    
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final CohortSubjectRepository cohortSubjectRepository;
    private final CohortEstimationService cohortEstimation;
    private final TeacherAssignmentService teacherAssignment;
    
    /**
     * Prepares all the data to be used by the solver.
     * Uses persisted data, only estimating as a fallback
     */
    @Transactional
    public PreSolverResult prepare(PreSolverRequest request) {
        log.info("=".repeat(60));
        log.info("PRE-SOLVER PREPARATION for {}.{}",
            request.academicYear(), request.semester());
        log.info("=".repeat(60));
        
        List<String> warnings = new ArrayList<>();
        int phantomTeachers = 0;
        int estimatedCohorts = 0;
        int cohortSubjectsCreated = 0;
        
        List<Course> courses = request.courseIds() != null
            ? courseRepository.findAllById(request.courseIds())
            : courseRepository.findAll();
        
        log.info("Processing {} courses", courses.size());
        
        List<CohortSubject> existingInSemester = cohortSubjectRepository
            .findByAcademicYearAndSemesterAndIsActive(
                request.academicYear(), 
                request.semester(), 
                true
            );
        
        for (Course course : courses) {
            CohortEstimationResult cohortResult = cohortEstimation.ensureCohortsExist(
                course,
                request.academicYear(),
                request.semester(),
                request.estimationConfig() != null
                    ? request.estimationConfig()
                    : CohortEstimationConfig.defaultConfig()
            );
            
            if (cohortResult.wasGenerated()) {
                estimatedCohorts += cohortResult.cohorts().size();
                warnings.addAll(cohortResult.warnings());
            }
            
            List<Subject> subjects = subjectRepository
                .search(null, null, request.semester(), course.getId()); 
            
            for (Cohort cohort : cohortResult.cohorts()) {
                
                List<Subject> yearSubjects = subjects.stream()
                    .filter(s -> s.getTargetYear() == cohort.getYear())
                    .toList();
                
                for (Subject subject : yearSubjects) {
                    boolean exists = cohortSubjectRepository
                        .existsByCohortAndSubjectAndAcademicYearAndSemester(
                            cohort, subject,
                            request.academicYear(), request.semester());
                    
                    if (exists) {
                        log.debug("CohortSubject already exists: {} - {}",
                            cohort.getDisplayName(), subject.getName());
                        continue;
                    }
                    
                    TeacherAssignmentResult assignment = teacherAssignment.assignTeacher(
                        subject, existingInSemester, request.policy());
                    
                    if (assignment.warning() != null) {
                        warnings.add(assignment.warning());
                    }
                    if (assignment.isPhantom()) {
                        phantomTeachers++;
                    }
                    
                    // Criar CohortSubject
                    CohortSubject cs = CohortSubject.builder()
                        .cohort(cohort)
                        .subject(subject)
                        .semester(request.semester())
                        .isActive(true)
                        .build();
                    
                    cohortSubjectRepository.save(cs);
                    existingInSemester.add(cs); // Atualizar carga
                    cohortSubjectsCreated++;
                    
                    log.debug("Created CohortSubject: {} → {} ({})",
                        cohort.getDisplayName(),
                        subject.getName(),
                        assignment.teacher().getUsername());
                }
            }
        }
        
        log.info("=".repeat(60));
        log.info("PREPARATION DONE!");
        log.info("Estimated cohorts created: {}", estimatedCohorts);
        log.info("CohortSubjects created: {}", cohortSubjectsCreated);
        log.info("Phantom teachers: {}", phantomTeachers);
        log.info("Warnings: {}", warnings.size());
        log.info("=".repeat(60));
        
        return new PreSolverResult(
            estimatedCohorts,
            cohortSubjectsCreated,
            phantomTeachers,
            warnings
        );
    }
}

