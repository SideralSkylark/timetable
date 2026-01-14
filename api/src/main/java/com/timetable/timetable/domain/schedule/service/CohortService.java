package com.timetable.timetable.domain.schedule.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timetable.timetable.domain.schedule.dto.CreateCohortRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateCohortRequest;
import com.timetable.timetable.domain.schedule.entity.Cohort;
import com.timetable.timetable.domain.schedule.entity.Course;
import com.timetable.timetable.domain.schedule.exception.CohortNotFoundException;
import com.timetable.timetable.domain.schedule.repository.CohortRepository;
import com.timetable.timetable.domain.user.entity.ApplicationUser;
import com.timetable.timetable.domain.user.entity.UserRole;
import com.timetable.timetable.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CohortService {
    private final CohortRepository cohortRepository;
    private final CourseService courseService;
    private final UserService userService;

    @Transactional
    public Cohort createCohort(CreateCohortRequest createRequest) {
        log.debug("Creating cohort");
        
        // Verificação de unicidade atualizada para incluir semester
        if (cohortRepository.existsByYearAndSectionAndSemesterAndAcademicYearAndCourseId(
            createRequest.year(), 
            createRequest.section(),
            createRequest.semester(),
            createRequest.academicYear(), 
            createRequest.courseId())
        ) {
            String cohortIdentifier = String.format("%d-%s-%d-%d", 
                createRequest.year(), 
                createRequest.section(), 
                createRequest.semester(),
                createRequest.academicYear());
            log.warn("Another cohort already exists with specification: {}", cohortIdentifier);
            throw new IllegalStateException(
                String.format("Cohort '%s' already exists for the designated course", cohortIdentifier)
            );
        }

        Course course = courseService.getById(createRequest.courseId());

        Set<ApplicationUser> students = new HashSet<>();
        if (createRequest.studentIds() != null && !createRequest.studentIds().isEmpty()) {
            students = validateAndFetchStudents(createRequest.studentIds());
        }

        Cohort cohort = Cohort.builder()
            .year(createRequest.year())
            .section(createRequest.section())
            .semester(createRequest.semester())
            .academicYear(createRequest.academicYear()) 
            .course(course)
            .courseNameSnapshot(course.getName())
            .students(students)
            .build();
        
        Cohort saved = cohortRepository.save(cohort);

        log.info("Cohort {} created with identifier: {}", saved.getId(), saved.getDisplayName());
        return saved;
    }

    public Page<Cohort> getAll(Pageable pageable) {
        log.debug("Fetching all cohorts");
        return cohortRepository.findAll(pageable);
    }

    public Cohort getById(Long id) {
        log.debug("Looking for cohort {}", id);
        Cohort cohort = cohortRepository.findById(id)
            .orElseThrow(() -> new CohortNotFoundException(
                String.format("Cohort with id %d not found", id)
            ));

        log.info("Cohort {} found: {}", id, cohort.getDisplayName());
        return cohort;
    }

    public List<Cohort> getCohortsByCourse(Long courseId) {
        log.debug("Fetching cohorts for course {}", courseId);
        return cohortRepository.findByCourseId(courseId);
    }
    
    public List<Cohort> getCohortsByYearAndSemester(int year, int semester) {
        log.debug("Fetching cohorts for year {} and semester {}", year, semester);
        return cohortRepository.findByYearAndSemester(year, semester);
    }
    
    public List<Cohort> getCohortsByAcademicYear(int academicYear) {
        log.debug("Fetching cohorts for academic year {}", academicYear);
        return cohortRepository.findByAcademicYear(academicYear);
    }

    @Transactional
    public Cohort updateCohort(Long id, UpdateCohortRequest updateRequest) {
        log.debug("Updating cohort {}", id);
        Cohort cohort = cohortRepository.findById(id)
            .orElseThrow(() -> new CohortNotFoundException(
                String.format("Cohort with id %d not found", id)
            ));

        // Verificação atualizada para incluir semester
        if (cohortRepository.existsAnotherWithSameAttributes(
            updateRequest.year(), 
            updateRequest.section(),
            updateRequest.semester(),
            updateRequest.academicYear(), 
            cohort.getCourse().getId(), 
            cohort.getId())
        ) {
            log.warn("Another cohort with the same data already exists");
            throw new IllegalArgumentException(
                "Another cohort with the same specification already exists"
            );
        }

        Set<ApplicationUser> students = validateAndFetchStudents(updateRequest.studentIds());

        cohort.setYear(updateRequest.year());
        cohort.setSection(updateRequest.section());
        cohort.setSemester(updateRequest.semester());
        cohort.setAcademicYear(updateRequest.academicYear());
        cohort.setStudents(students);

        Cohort updated = cohortRepository.save(cohort);

        log.info("Cohort {} updated: {}", updated.getId(), updated.getDisplayName());
        return updated;
    }

    @Transactional
    public void deleteCohort(Long id) {
        log.debug("Deleting cohort {}", id);
        if (!cohortRepository.existsById(id)) {
            log.warn("Cohort {} not found", id);
            throw new CohortNotFoundException(
                String.format("Cohort with id %d not found", id)
            );
        }
        cohortRepository.deleteById(id);

        log.info("Cohort {} deleted", id);
    }

    private Set<ApplicationUser> validateAndFetchStudents(List<Long> studentIds) {
        Set<ApplicationUser> students = new HashSet<>();
        
        for (Long studentId : studentIds) {
            ApplicationUser user = userService.getUserById(studentId);
     
            if (!user.hasRole(UserRole.STUDENT)) {
                throw new IllegalArgumentException(
                    String.format("User with id %d is not a student", studentId)
                );
            }
            
            students.add(user);
        }
        
        return students;
    }
}
