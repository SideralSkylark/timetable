package com.timetable.timetable.domain.schedule.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timetable.timetable.domain.schedule.dto.CreateCohortSubjectRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateCohortSubjectRequest;
import com.timetable.timetable.domain.schedule.entity.Cohort;
import com.timetable.timetable.domain.schedule.entity.CohortSubject;
import com.timetable.timetable.domain.schedule.entity.Subject;
import com.timetable.timetable.domain.schedule.exception.CohortSubjectNotFoundException;
import com.timetable.timetable.domain.schedule.repository.CohortSubjectRepository;
import com.timetable.timetable.domain.user.entity.ApplicationUser;
import com.timetable.timetable.domain.user.entity.UserRole;
import com.timetable.timetable.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CohortSubjectService {
    private final CohortSubjectRepository cohortSubjectRepository;
    private final CohortService cohortService;
    private final SubjectService subjectService;
    private final UserService userService;

    @Transactional
    public CohortSubject createCohortSubject(CreateCohortSubjectRequest request) {
        log.debug("Creating cohort subject assignment");
        
        // Buscar entidades relacionadas
        Cohort cohort = cohortService.getById(request.cohortId());
        Subject subject = subjectService.getById(request.subjectId());
        ApplicationUser teacher = userService.getUserById(request.assignedTeacherId());
        
        // Validações básicas
        validateTeacherIsEligible(teacher, subject);
        validateCohortSubjectCompatibility(cohort, subject);
        
        // Verificar se já existe atribuição para esta combinação
        if (cohortSubjectRepository.existsByCohortAndSubjectAndAcademicYearAndSemester(
            cohort, subject, cohort.getAcademicYear(), cohort.getSemester())) {
            log.warn("Cohort subject assignment already exists for cohort {}, subject {}, year {}, semester {}",
                cohort.getId(), subject.getId(), cohort.getAcademicYear(), cohort.getSemester());
            throw new IllegalStateException(
                String.format("This subject is already assigned to this cohort for academic year %d semester %d",
                    cohort.getAcademicYear(), cohort.getSemester())
            );
        }
        
        // Validar que o professor está disponível (sem sobrecarga)
        validateTeacherWorkload(teacher, request.weeklyHours());
        
        // Construir a entidade
        CohortSubject cohortSubject = CohortSubject.builder()
            .cohort(cohort)
            .subject(subject)
            .assignedTeacher(teacher)
            .academicYear(cohort.getAcademicYear())
            .semester(cohort.getSemester())
            .weeklyHours(request.weeklyHours())
            .lessonsPerWeek(request.lessonsPerWeek())
            .isActive(true)
            .build();
        
        // Validar consistência
        if (!cohortSubject.isValid()) {
            throw new IllegalArgumentException("Invalid cohort subject assignment");
        }
        
        CohortSubject saved = cohortSubjectRepository.save(cohortSubject);
        
        log.info("Cohort subject assignment {} created: {}", 
            saved.getId(), saved.getDisplayName());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<CohortSubject> getAll(Pageable pageable) {
        log.debug("Fetching all cohort subject assignments");
        return cohortSubjectRepository.findAllWithDetails(pageable);
    }

    @Transactional(readOnly = true)
    public Page<CohortSubject> getByCohort(Long cohortId, Pageable pageable) {
        log.debug("Fetching cohort subject assignments for cohort {}", cohortId);
        Cohort cohort = cohortService.getById(cohortId);
        return cohortSubjectRepository.findByCohortWithDetails(cohort, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CohortSubject> getBySubject(Long subjectId, Pageable pageable) {
        log.debug("Fetching cohort subject assignments for subject {}", subjectId);
        Subject subject = subjectService.getById(subjectId);
        return cohortSubjectRepository.findBySubjectWithDetails(subject, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CohortSubject> getByTeacher(Long teacherId, Pageable pageable) {
        log.debug("Fetching cohort subject assignments for teacher {}", teacherId);
        ApplicationUser teacher = userService.getUserById(teacherId);
        
        if (!teacher.hasRole(UserRole.TEACHER)) {
            throw new IllegalArgumentException(
                String.format("User with id %d is not a teacher", teacherId)
            );
        }
        
        return cohortSubjectRepository.findByAssignedTeacherWithDetails(teacher, pageable);
    }

    @Transactional(readOnly = true)
    public CohortSubject getById(Long id) {
        log.debug("Fetching cohort subject assignment {}", id);
        return cohortSubjectRepository.findWithDetailsById(id)
            .orElseThrow(() -> new CohortSubjectNotFoundException(
                String.format("Cohort subject assignment with id %d not found", id)
            ));
    }

    @Transactional
    public CohortSubject updateCohortSubject(Long id, UpdateCohortSubjectRequest request) {
        log.debug("Updating cohort subject assignment {}", id);
        CohortSubject cohortSubject = getById(id);
        
        // Se estiver mudando o professor
        if (!cohortSubject.getAssignedTeacher().getId().equals(request.assignedTeacherId())) {
            ApplicationUser newTeacher = userService.getUserById(request.assignedTeacherId());
            validateTeacherIsEligible(newTeacher, cohortSubject.getSubject());
            validateTeacherWorkload(newTeacher, request.weeklyHours());
            cohortSubject.setAssignedTeacher(newTeacher);
        }
        
        // Atualizar horas e aulas
        cohortSubject.setWeeklyHours(request.weeklyHours());
        cohortSubject.setLessonsPerWeek(request.lessonsPerWeek());
        cohortSubject.setActive(request.isActive());
        
        // Validar consistência
        if (!cohortSubject.isValid()) {
            throw new IllegalArgumentException("Invalid cohort subject assignment after update");
        }
        
        CohortSubject updated = cohortSubjectRepository.save(cohortSubject);
        
        log.info("Cohort subject assignment {} updated", updated.getId());
        return updated;
    }

    @Transactional
    public void deleteCohortSubject(Long id) {
        log.debug("Deleting cohort subject assignment {}", id);
        if (!cohortSubjectRepository.existsById(id)) {
            throw new CohortSubjectNotFoundException(
                String.format("Cohort subject assignment with id %d not found", id)
            );
        }
        
        // Verificar se existem ScheduledClasses associadas
        int scheduledClassesCount = cohortSubjectRepository.countScheduledClassesByCohortSubjectId(id);
        if (scheduledClassesCount > 0) {
            throw new IllegalStateException(
                String.format("Cannot delete cohort subject assignment with %d scheduled classes", 
                    scheduledClassesCount)
            );
        }
        
        cohortSubjectRepository.deleteById(id);
        log.info("Cohort subject assignment {} deleted", id);
    }

    @Transactional(readOnly = true)
    public List<CohortSubject> getSubjectsForCohortSchedule(Long cohortId) {
        log.debug("Fetching active subjects for cohort {} scheduling", cohortId);
        Cohort cohort = cohortService.getById(cohortId);
        return cohortSubjectRepository.findActiveByCohort(cohort);
    }

    @Transactional(readOnly = true)
    public List<CohortSubject> getByAcademicYearAndSemester(int academicYear, int semester) {
        log.debug("Fetching cohort subject assignments for academic year {} semester {}", 
            academicYear, semester);
        return cohortSubjectRepository.findByAcademicYearAndSemester(academicYear, semester);
    }

    private void validateTeacherIsEligible(ApplicationUser teacher, Subject subject) {
        if (!teacher.hasRole(UserRole.TEACHER)) {
            throw new IllegalArgumentException(
                String.format("User with id %d is not a teacher", teacher.getId())
            );
        }
        
        if (!subject.getEligibleTeachers().contains(teacher)) {
            throw new IllegalArgumentException(
                String.format("Teacher %s is not eligible to teach subject %s", 
                    teacher.getUsername(), subject.getName())
            );
        }
    }

    private void validateCohortSubjectCompatibility(Cohort cohort, Subject subject) {
        if (cohort.getYear() != subject.getTargetYear()) {
            throw new IllegalArgumentException(
                String.format("Subject target year (%d) does not match cohort year (%d)",
                    subject.getTargetYear(), cohort.getYear())
            );
        }
        
        if (cohort.getSemester() != subject.getTargetSemester()) {
            throw new IllegalArgumentException(
                String.format("Subject target semester (%d) does not match cohort semester (%d)",
                    subject.getTargetSemester(), cohort.getSemester())
            );
        }
        
        if (!cohort.getCourse().getId().equals(subject.getCourse().getId())) {
            throw new IllegalArgumentException(
                "Subject does not belong to the same course as the cohort"
            );
        }
    }

    private void validateTeacherWorkload(ApplicationUser teacher, int additionalHours) {
        // Implementar lógica de validação de carga horária do professor
        // Exemplo: não permitir mais de 40 horas semanais
        int currentWeeklyHours = cohortSubjectRepository.sumWeeklyHoursByTeacher(teacher);
        
        if (currentWeeklyHours + additionalHours > 40) { // Limite de 40 horas semanais
            throw new IllegalArgumentException(
                String.format("Teacher would exceed maximum weekly hours (current: %d, additional: %d, max: 40)",
                    currentWeeklyHours, additionalHours)
            );
        }
    }
    
    @Transactional(readOnly = true)
    public int getTotalWeeklyHoursForCohort(Long cohortId) {
        Cohort cohort = cohortService.getById(cohortId);
        return cohortSubjectRepository.sumWeeklyHoursByCohort(cohort);
    }
}
