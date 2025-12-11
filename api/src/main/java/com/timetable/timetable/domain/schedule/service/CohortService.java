package com.timetable.timetable.domain.schedule.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timetable.timetable.domain.schedule.dto.CohortResponse;
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

@Service
@RequiredArgsConstructor
public class CohortService {
    private final CohortRepository cohortRepository;
    private final CourseService courseService;
    private final UserService userService;

    @Transactional
    public Cohort createCohort(CreateCohortRequest createRequest) {
        if (cohortRepository.existsByName(createRequest.name())) {
            throw new IllegalStateException("Cohort with name '%s' already exists".formatted(createRequest.name()));
        }

        Course course = courseService.getCourseById(createRequest.courseId());

        Set<ApplicationUser> students = new HashSet<>();
        if (createRequest.studentIds() != null && !createRequest.studentIds().isEmpty()) {
            students = validateAndFetchStudents(createRequest.studentIds());
        }

        Cohort cohort = Cohort.builder()
            .name(createRequest.name())
            .course(course)
            .students(students)
            .build();
        
        Cohort saved = cohortRepository.save(cohort);
        return saved;
    }

    public Page<Cohort> getAll(Pageable pageable) {
        return cohortRepository.findAll(pageable);
    }

    public Cohort getById(Long id) {
        Cohort cohort = cohortRepository.findById(id)
            .orElseThrow(() -> new CohortNotFoundException(
                "Cohort with id %d not found".formatted(id)
            ));

        return cohort;
    }

    @Transactional
    public Cohort updateCohort(Long id, UpdateCohortRequest updateRequest) {
        Cohort cohort = cohortRepository.findById(id)
            .orElseThrow(() -> new CohortNotFoundException(
                "Cohort with id %d not found".formatted(id)
            ));

        if (!cohort.getName().equals(updateRequest.name()) && 
            cohortRepository.existsByName(updateRequest.name())) {
            throw new IllegalArgumentException(
                "Another cohort with name '%s' already exists".formatted(updateRequest.name())
            );
        }

        Set<ApplicationUser> students = validateAndFetchStudents(updateRequest.studentIds());

        cohort.setName(updateRequest.name());
        cohort.setStudents(students);

        Cohort updated = cohortRepository.save(cohort);
        return updated;
    }

    @Transactional
    public void deleteCohort(Long id) {
        if (!cohortRepository.existsById(id)) {
            throw new CohortNotFoundException(
                "Cohort with id %d not found".formatted(id)
            );
        }
        cohortRepository.deleteById(id);
    }

    private Set<ApplicationUser> validateAndFetchStudents(List<Long> studentIds) {
        Set<ApplicationUser> students = new HashSet<>();
        
        for (Long studentId : studentIds) {
            ApplicationUser user = userService.getUserById(studentId);
     
            if (!user.hasRole(UserRole.STUDENT)) {
                throw new IllegalArgumentException(
                    "User with id %d is not a student".formatted(studentId)
                );
            }
            
            students.add(user);
        }
        
        return students;
    }
}
