package com.timetable.timetable.domain.schedule.service;

import com.timetable.timetable.domain.schedule.dto.CourseResponse;
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

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserService userService;

    public CourseResponse createCourse(CreateCourseRequest createRequest) {
        if (courseRepository.existsByName(createRequest.name())) {
            throw new IllegalStateException("Course already exists"); 
        }
        
        ApplicationUser coordinator = userService.getUserEntityById(createRequest.coordinatorId());

        if (!coordinator.hasRole(UserRole.COORDINATOR)) {
            throw new IllegalArgumentException(
                "User %d is not a coordinator".formatted(createRequest.coordinatorId())
            );
        }

        Course course = Course.builder()
            .name(createRequest.name())
            .coordinator(coordinator)
            .build();

        Course saved = courseRepository.save(course);

        return CourseResponse.from(saved);
    }

    public Page<CourseResponse> getAll(Pageable pageable) {
        return courseRepository.findAll(pageable)
            .map(CourseResponse::from);
    }

    public CourseResponse getById(Long id) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new CourseNotFoundException("No course with id: %d".formatted(id)));

        return CourseResponse.from(course);        
    }

    public Course getCourseById(Long id) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new CourseNotFoundException("No course with id: %d".formatted(id)));

        return course;        
    }

    public CourseResponse updateCourse(Long id, UpdateCourseRequest updateRequest) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new CourseNotFoundException("No course found with id: %d".formatted(id)));

        if (!course.getName().equals(updateRequest.name()) && courseRepository.existsByName(updateRequest.name())) {
            throw new IllegalArgumentException("Another course with that name already exists.");
        }
        
        ApplicationUser coordinator = userService.getUserEntityById(updateRequest.coordinatorId());

        if (!coordinator.hasRole(UserRole.COORDINATOR)) {
            throw new IllegalArgumentException(
                "User %d is not a coordinator".formatted(updateRequest.coordinatorId())
            );
        }

        course.setName(updateRequest.name());
        course.setCoordinator(coordinator);

        Course updated = courseRepository.save(course);

        return CourseResponse.from(updated);
    }

    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException(
                "No course with id: %d".formatted(id)
            );
        }
        courseRepository.deleteById(id);
    }
}
