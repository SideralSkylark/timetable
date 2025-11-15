package com.timetable.timetable.domain.schedule.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateRoomRequest(
    @NotBlank(message = "Name must not be empty.") 
    String name,

    @NotBlank(message = "Capacity must not be empty.")
    @Min(value = 1, message = "Capacity must be at leat 1.")
    int capacity
) { }
