package com.timetable.timetable.domain.schedule.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRoomRequest(
    @NotBlank(message = "Name must not be empty.") 
    String name,

    @NotNull(message = "Capacity must not be empty.")
    @Min(value = 1, message = "Capacity must be at leat 1.")
    int capacity,

    @NotNull(message = "course restriction must be specified")
    Long courseId
) { }
