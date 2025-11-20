package com.timetable.timetable.domain.schedule.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timetable.timetable.domain.schedule.dto.CreateSubjectRequest;
import com.timetable.timetable.domain.schedule.dto.SubjectResponse;
import com.timetable.timetable.domain.schedule.dto.UpdateSubjectRequest;
import com.timetable.timetable.domain.schedule.entity.Course;
import com.timetable.timetable.domain.schedule.entity.Subject;
import com.timetable.timetable.domain.schedule.exception.SubjectNotFoundException;
import com.timetable.timetable.domain.schedule.repository.SubjectRepository;
import com.timetable.timetable.domain.user.entity.ApplicationUser;
import com.timetable.timetable.domain.user.entity.UserRole;
import com.timetable.timetable.domain.user.exception.UserNotFoundException;
import com.timetable.timetable.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final CourseService courseService;
    private final UserService userService;

    @Transactional
    public SubjectResponse createSubject(CreateSubjectRequest createRequest) {
        Course course = courseService.getCourseById(createRequest.courseId());

        if (subjectRepository.existsByNameAndCourse(createRequest.name(), course)) {
            throw new IllegalStateException(
                "Subject with name '%s' already exists in this course".formatted(createRequest.name())
            );
        }

        Set<ApplicationUser> teachers = new HashSet<>();
        if (createRequest.teacherIds() != null && !createRequest.teacherIds().isEmpty()) {
            teachers = validateAndFetchTeachers(createRequest.teacherIds());
        }

        Subject subject = Subject.builder()
            .name(createRequest.name())
            .course(course)
            .teachers(teachers)
            .build();

        Subject saved = subjectRepository.save(subject);
        return SubjectResponse.from(saved);
    }

    public Page<SubjectResponse> getAll(Pageable pageable) {
        return subjectRepository.findAll(pageable)
            .map(SubjectResponse::from);
    }

    public Page<SubjectResponse> getAllByCourse(Long courseId, Pageable pageable) {
        Course course = courseService.getCourseById(courseId);
        
        return subjectRepository.findByCourse(course, pageable)
            .map(SubjectResponse::from);
    }

    public SubjectResponse getById(Long id) {
        Subject subject = subjectRepository.findById(id)
            .orElseThrow(() -> new SubjectNotFoundException(
                "Subject with id %d not found".formatted(id)
            ));
        return SubjectResponse.from(subject);
    }

    public Subject getSubjectById(Long id) {
        Subject subject = subjectRepository.findById(id)
            .orElseThrow(() -> new SubjectNotFoundException(
                "Subject with id %d not found".formatted(id)
            ));
        return subject;
    }

    @Transactional
    public SubjectResponse updateSubject(Long id, UpdateSubjectRequest updateRequest) {
        Subject subject = subjectRepository.findById(id)
            .orElseThrow(() -> new SubjectNotFoundException(
                "Subject with id %d not found".formatted(id)
            ));

        if (!subject.getName().equals(updateRequest.name()) && 
            subjectRepository.existsByNameAndCourse(updateRequest.name(), subject.getCourse())) {
            throw new IllegalArgumentException(
                "Another subject with name '%s' already exists in this course".formatted(updateRequest.name())
            );
        }

        Set<ApplicationUser> teachers = new HashSet<>();
        if (updateRequest.teacherIds() != null && !updateRequest.teacherIds().isEmpty()) {
            teachers = validateAndFetchTeachers(updateRequest.teacherIds());
        }

        subject.setName(updateRequest.name());
        subject.setTeachers(teachers);

        Subject updated = subjectRepository.save(subject);
        return SubjectResponse.from(updated);
    }

    @Transactional
    public void deleteSubject(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new SubjectNotFoundException(
                "Subject with id %d not found".formatted(id)
            );
        }
        subjectRepository.deleteById(id);
    }

    private Set<ApplicationUser> validateAndFetchTeachers(List<Long> teacherIds) {
        Set<ApplicationUser> teachers = new HashSet<>();
        
        for (Long teacherId : teacherIds) {
            ApplicationUser user = userService.getUserEntityById(teacherId);
            
            if (!user.hasRole(UserRole.TEACHER)) {
                throw new IllegalArgumentException(
                    "User with id %d is not a teacher".formatted(teacherId)
                );
            }
            
            teachers.add(user);
        }
        
        return teachers;
    }
}
