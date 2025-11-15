package com.timetable.timetable.domain.schedule.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateRoomRequest (
    @NotBlank(message = "Name must not be blank.") 
    String name,

    @Min(value = 1, message = "Capacity must be at least one.")
    int capacity
) {

}
