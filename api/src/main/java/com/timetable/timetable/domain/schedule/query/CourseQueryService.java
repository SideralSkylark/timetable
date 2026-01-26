package com.timetable.timetable.domain.schedule.query;

import com.timetable.timetable.domain.schedule.dto.CoordinatorOption;
import com.timetable.timetable.domain.schedule.dto.CourseListResponse;
import com.timetable.timetable.domain.schedule.entity.Course;
import com.timetable.timetable.domain.schedule.exception.CourseNotFoundException;
import com.timetable.timetable.domain.schedule.repository.CourseRepository;
import com.timetable.timetable.domain.schedule.repository.SubjectRepository;
import com.timetable.timetable.domain.user.entity.UserRole;
import com.timetable.timetable.domain.user.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CourseQueryService {
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final UserService userService;

    public Page<Course> getAll(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    public Page<CourseListResponse> findAllWithSubjectCount(Pageable pageable) {
        Page<Course> courses = courseRepository.findAll(pageable);
        return courses.map(course -> {
            Long subjectCount = subjectRepository.countByCourseId(course.getId());
            return CourseListResponse.from(course, subjectCount);
        });
    }

    public Course getById(Long id) {
        log.debug("Looking for course {}", id);
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new CourseNotFoundException("No course with id: %d".formatted(id)));

        log.info("Course {} found", id);
        return course;
    }

    public Page<CoordinatorOption> getAvailableCoordinators(Pageable pageable) {
        return userService.getUsersByRole(UserRole.COORDINATOR, pageable).map(CoordinatorOption::from);
    }
}
