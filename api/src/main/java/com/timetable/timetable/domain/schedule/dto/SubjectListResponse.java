package com.timetable.timetable.domain.schedule.dto;

import java.util.List;

import com.timetable.timetable.domain.schedule.entity.Subject;

public record SubjectListResponse(
    Long id,
    String name,
    List<TeacherInfo> teachers
) {
    public static SubjectListResponse from(Subject subject) {
        return new SubjectListResponse(
            subject.getId(),
            subject.getName(),
            subject.getTeachers().stream()
                .map(t -> new TeacherInfo(t.getId(), t.getUsername()))
                .toList() 
        );
    }
    
    public record TeacherInfo(
        Long id,
        String name
    ) {}
}
