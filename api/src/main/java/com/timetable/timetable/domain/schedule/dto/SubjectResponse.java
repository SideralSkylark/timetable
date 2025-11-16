package com.timetable.timetable.domain.schedule.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.timetable.timetable.domain.schedule.entity.Subject;

public record SubjectResponse(
    Long id,
    String name,
    Long courseId,
    String courseName,
    List<TeacherInfo> teachers
) {
    public static SubjectResponse from(Subject subject) {
        return new SubjectResponse(
            subject.getId(),
            subject.getName(),
            subject.getCourse().getId(),
            subject.getCourse().getName(),
            subject.getTeachers() != null 
                ? subject.getTeachers().stream()
                    .map(teacher -> new TeacherInfo(
                        teacher.getId(),
                        teacher.getUsername()
                    ))
                    .collect(Collectors.toList())
                : List.of()
        );
    }
    
    public record TeacherInfo(
        Long id,
        String name
    ) {}
}
