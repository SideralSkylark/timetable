package com.timetable.timetable.scheduler_engine.preparation;

import com.timetable.timetable.domain.schedule.dto.CohortEstimationConfig;
import com.timetable.timetable.domain.schedule.dto.CohortEstimationResult;
import com.timetable.timetable.domain.schedule.entity.*;
import com.timetable.timetable.domain.schedule.repository.*;
import com.timetable.timetable.domain.user.entity.ApplicationUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CohortEstimationService {
    
    private final CohortRepository cohortRepository;
    
    /**
     * Garante que existem cohorts para o período especificado.
     * Se não existirem, cria cohorts estimados baseado nas regras.
     */
    @Transactional
    public CohortEstimationResult ensureCohortsExist(
            Course course,
            int academicYear,
            int semester,
            CohortEstimationConfig config) {
        
        List<Cohort> existing = cohortRepository
            .findByCriteria(null, semester, academicYear, course.getId());
        
        if (!existing.isEmpty()) {
            log.info("Found {} existing cohorts for {} {}.{}",
                existing.size(), course.getName(), academicYear, semester);
            return new CohortEstimationResult(existing, new ArrayList<>(), false);
        }
        
        // Não existem cohorts → Gerar estimados
        log.info("No cohorts found for {} {}.{}. Generating estimates...",
            course.getName(), academicYear, semester);
        
        List<Cohort> generated = generateEstimatedCohorts(
            course, academicYear, semester, config);
        
        cohortRepository.saveAll(generated);
        
        log.info("Generated {} estimated cohorts for {}",
            generated.size(), course.getName());
        
        List<String> warnings = generated.stream()
            .map(c -> String.format("Estimated cohort created: %s (est. %d students)",
                c.getDisplayName(), c.getEstimatedStudentCount()))
            .toList();
        
        return new CohortEstimationResult(generated, warnings, true);
    }
    
    /**
     * Confirma cohorts quando dados reais chegam (ingressos)
     */
    @Transactional
    public void confirmCohort(Long cohortId, Set<ApplicationUser> realStudents) {
        Cohort cohort = cohortRepository.findById(cohortId)
            .orElseThrow(() -> new IllegalArgumentException("Cohort not found: " + cohortId));
        
        cohort.setStudents(realStudents);
        cohort.setStatus(CohortStatus.CONFIRMED);
        cohortRepository.save(cohort);
        
        log.info("Cohort {} confirmed with {} real students",
            cohort.getDisplayName(), realStudents.size());
    }
    
    // ========================================
    // GERAÇÃO DE COHORTS ESTIMADOS
    // ========================================
    
    private List<Cohort> generateEstimatedCohorts(
            Course course,
            int academicYear,
            int semester,
            CohortEstimationConfig config) {
        
        List<Cohort> cohorts = new ArrayList<>();
        
        for (int year = 1; year <= config.courseYears(); year++) {
            
            int cohortsForYear = config.cohortsForYear(year);
            
            for (int i = 0; i < cohortsForYear; i++) {
                String section = String.valueOf((char) ('A' + i));
                
                Cohort cohort = Cohort.builder()
                    .year(year)
                    .section(section)
                    .academicYear(academicYear)
                    .semester(semester)
                    .course(course)
                    .courseNameSnapshot(course.getName())
                    .status(CohortStatus.ESTIMATED)
                    .estimatedStudentCount(config.estimatedStudentsPerCohort())
                    .students(new HashSet<>())
                    .build();
                
                cohorts.add(cohort);
                log.debug("Generated: {} ({} est. students)", 
                    cohort.getDisplayName(), config.estimatedStudentsPerCohort());
            }
        }
        
        return cohorts;
    }
}

