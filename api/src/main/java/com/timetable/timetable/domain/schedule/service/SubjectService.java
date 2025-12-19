package com.timetable.timetable.domain.schedule.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timetable.timetable.domain.schedule.dto.CreateSubjectRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateSubjectRequest;
import com.timetable.timetable.domain.schedule.entity.Course;
import com.timetable.timetable.domain.schedule.entity.Subject;
import com.timetable.timetable.domain.schedule.exception.SubjectNotFoundException;
import com.timetable.timetable.domain.schedule.repository.SubjectRepository;
import com.timetable.timetable.domain.user.entity.ApplicationUser;
import com.timetable.timetable.domain.user.entity.UserRole;
import com.timetable.timetable.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final CourseService courseService;
    private final UserService userService;

    @Transactional
    public Subject createSubject(CreateSubjectRequest request) {
        log.debug("Creating subject");
        Course course = courseService.getById(request.courseId());

        if (subjectRepository.existsByNameAndCourse(request.name(), course)) {
            log.warn("Subject already exists with this name for this course");
            throw new IllegalStateException(
                "Subject with name '%s' already exists in this course".formatted(request.name())
            );
        }

        Subject subject = Subject.builder()
            .name(request.name())
            .course(course)
            .teachers(fetchTeachers(request.teacherIds()))
            .build();

        Subject saved = subjectRepository.save(subject);

        log.info("Subject {} created", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Subject> getAllByCourse(Long courseId, Pageable pageable) {
        log.debug("Fetching all subjects for course {}", courseId);

        Course course = courseService.getById(courseId);

        Page<Subject> page = subjectRepository.findByCourse(course, pageable);

        if (!page.isEmpty()) {
            subjectRepository.findByIdIn(
                page.getContent().stream()
                .map(Subject::getId)
                .toList()
            );
        }

        return page;
    }

    // @Transactional(readOnly = true)
    // public Subject getById(Long id) {
    //     log.debug("fetching subject {}", id);
    //     return subjectRepository.findWithDetailsById(id);
    // }


    public Subject getById(Long id) {
        log.debug("Fetching subject {}", id);
        Subject subject = subjectRepository.findById(id)
            .orElseThrow(() -> new SubjectNotFoundException(
                "Subject with id %d not found".formatted(id)
            ));

        log.info("Found subject {}", id);
        return subject;
    }

    @Transactional
    public Subject updateSubject(Long id, UpdateSubjectRequest request) {
        log.debug("Updating subject {}", id);
        Subject subject = getById(id);

        if (!subject.getName().equals(request.name()) && 
            subjectRepository.existsByNameAndCourse(request.name(), subject.getCourse())) {
            log.debug("Another subject with this name already exists for this course");
            throw new IllegalArgumentException(
                "Another subject with name '%s' already exists in this course".formatted(request.name())
            );
        }


        subject.setName(request.name());
        subject.setTeachers(fetchTeachers(request.teacherIds()));

        Subject updated = subjectRepository.save(subject);

        log.info("Updated subject {}", updated.getId());
        return updated;
    }

    @Transactional
    public void deleteSubject(Long id) {
        log.debug("Deleting subject {}", id);
        if (!subjectRepository.existsById(id)) {
            throw new SubjectNotFoundException(
                "Subject with id %d not found".formatted(id)
            );
        }

        subjectRepository.deleteById(id);
        log.info("Subject {} deleted", id);
    }

    private Set<ApplicationUser> fetchTeachers(List<Long> ids) {
        Set<ApplicationUser> teachers = new HashSet<>();

        if (ids == null) return teachers;
        
        for (Long id : ids) {
            ApplicationUser user = userService.getUserById(id);
            
            if (!user.hasRole(UserRole.TEACHER)) {
                throw new IllegalArgumentException("User with id %d is not a teacher".formatted(id));
            }
            
            teachers.add(user);
        }
        
        return teachers;
    }
}
