package com.timetable.timetable.domain.schedule.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSubjectRequest(
    @NotBlank(message = "Subject name is required and cannot be empty")
    @Size(min = 2, max = 150, message = "Subject name must be between 2 and 150 characters")
    String name,
    
    @NotNull(message = "Course ID is required")
    Long courseId,
    
    List<Long> teacherIds
) {}
