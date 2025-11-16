package com.timetable.timetable.domain.schedule.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSubjectRequest(
    @NotBlank(message = "Subject name is required and cannot be empty")
    @Size(min = 2, max = 150, message = "Subject name must be between 2 and 150 characters")
    String name,
    
    List<Long> teacherIds
) {}
