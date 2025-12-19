package com.timetable.timetable.domain.schedule.service;

import com.timetable.timetable.domain.schedule.dto.CreateCourseRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateCourseRequest;
import com.timetable.timetable.domain.schedule.entity.Course;
import com.timetable.timetable.domain.schedule.exception.CourseNotFoundException;
import com.timetable.timetable.domain.schedule.repository.CourseRepository;
import com.timetable.timetable.domain.user.entity.ApplicationUser;
import com.timetable.timetable.domain.user.entity.UserRole;
import com.timetable.timetable.domain.user.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                "User %d is not a coordinator".formatted(createRequest.coordinatorId())
            );
        }

        Course course = Course.builder()
            .name(createRequest.name())
            .coordinator(coordinator)
            .build();

        Course saved = courseRepository.save(course);

        log.info("Course {} created", saved.getId());
        return saved;
    }

    public Page<Course> getAll(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    public Course getById(Long id) {
        log.debug("Looking for course {}", id);
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new CourseNotFoundException("No course with id: %d".formatted(id)));

        log.info("Course {} found", id);
        return course;
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
                "User %d is not a coordinator".formatted(updateRequest.coordinatorId())
            );
        }

        course.setName(updateRequest.name());
        course.setCoordinator(coordinator);

        Course updated = courseRepository.save(course);

        log.info("Course {} updated", id);
        return updated;
    }

    public void deleteCourse(Long id) {
        log.debug("Deleting course {}", id);
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException(
                "No course with id: %d".formatted(id)
            );
        }
        courseRepository.deleteById(id);
        log.info("Course {} deleted", id);
    }
}
