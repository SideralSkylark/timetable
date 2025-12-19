package com.timetable.timetable.domain.schedule.dto;

import java.util.List;

import com.timetable.timetable.domain.schedule.entity.Subject;

public record SubjectDetailResponse(
    Long id,
    String name,
    Long courseId,
    String courseName,
    List<TeacherInfo> teachers
) {
    public static SubjectDetailResponse from(Subject subject) {
        return new SubjectDetailResponse(
            subject.getId(),
            subject.getName(),
            subject.getCourse().getId(),
            subject.getCourse().getName(),
            subject.getTeachers().stream()
                .map(t -> new TeacherInfo(t.getId(), t.getUsername()))
                .toList()
        );
    }

    public record TeacherInfo(Long id, String name) {}
}
