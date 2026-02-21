package com.timetable.timetable.domain.schedule.service;

import java.util.HashMap;
import java.util.Map;

import com.timetable.timetable.domain.schedule.dto.CreateCourseRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateCourseRequest;
import com.timetable.timetable.domain.schedule.entity.Course;
import com.timetable.timetable.domain.schedule.exception.CourseNotFoundException;
import com.timetable.timetable.domain.schedule.repository.CourseRepository;
import com.timetable.timetable.domain.user.entity.ApplicationUser;
import com.timetable.timetable.domain.user.entity.UserRole;
import com.timetable.timetable.domain.user.service.UserService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserService userService;

    public Course createCourse(CreateCourseRequest createRequest) {
        log.debug("Creating course");
        if (courseRepository.existsByName(createRequest.name())) {
            log.warn("Course already exists with name: {}", createRequest.name());
            throw new IllegalStateException("Course already exists");
        }

        ApplicationUser coordinator = userService.getUserById(createRequest.coordinatorId());

        if (!coordinator.hasRole(UserRole.COORDINATOR)) {
            log.warn("User {}, is not a coordinator", createRequest.coordinatorId());
            throw new IllegalArgumentException(
                    "User %d is not a coordinator".formatted(createRequest.coordinatorId()));
        }

        int years = createRequest.years() != null ? createRequest.years() : 4;
        Map<Integer, Integer> cohorts = createRequest.expectedCohortsPerYear() != null
                ? createRequest.expectedCohortsPerYear()
                : buildDefaultCohorts(years);

        validateCohortConfig(years, cohorts);

        Course course = Course.builder()
                .name(createRequest.name())
                .coordinator(coordinator)
                .years(years)
                .expectedCohortsPerYear(cohorts)
                .build();

        Course saved = courseRepository.save(course);

        log.info("Course {} created", saved.getId());
        return saved;
    }

    public Course getById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("No course with id: %d".formatted(id)));
    }

    public Course updateCourse(Long id, UpdateCourseRequest updateRequest) {
        log.debug("Updating course {}", id);
        Course course = getById(id);

        if (!course.getName().equals(updateRequest.name()) && courseRepository.existsByName(updateRequest.name())) {
            log.warn("Another course with that name already exists");
            throw new IllegalArgumentException("Another course with that name already exists.");
        }

        ApplicationUser coordinator = userService.getUserById(updateRequest.coordinatorId());

        if (!coordinator.hasRole(UserRole.COORDINATOR)) {
            log.warn("User {} is not a coordinator", updateRequest.coordinatorId());
            throw new IllegalArgumentException(
                    "User %d is not a coordinator".formatted(updateRequest.coordinatorId()));
        }

        course.setName(updateRequest.name());
        course.setCoordinator(coordinator);

        if (updateRequest.years() != null) {
            course.setYears(updateRequest.years());
        }

        if (updateRequest.expectedCohortsPerYear() != null) {
            course.setExpectedCohortsPerYear(updateRequest.expectedCohortsPerYear());
        }

        Course updated = courseRepository.save(course);

        log.info("Course {} updated", id);
        return updated;
    }

    public void deleteCourse(Long id) {
        log.debug("Deleting course {}", id);
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "No course with id: %d".formatted(id));
        }
        courseRepository.deleteById(id);
        log.info("Course {} deleted", id);
    }

    private Map<Integer, Integer> buildDefaultCohorts(int years) {
        Map<Integer, Integer> defaults = new HashMap<>();

        for (int year = 1; year <= years; year++) {
            defaults.put(year, 2);
        }

        return defaults;
    }

    private void validateCohortConfig(int years, Map<Integer, Integer> cohorts) {
        cohorts.forEach((year, value) -> {
            if (year > years) {
                throw new IllegalArgumentException(
                        "Cohort configuration contains invalid year: " + year);
            }

            if (value <= 0) {
                throw new IllegalArgumentException(
                        "Cohort number must be positive");
            }
        });
    }
}
