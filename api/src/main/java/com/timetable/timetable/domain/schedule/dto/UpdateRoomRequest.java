package com.timetable.timetable.domain.schedule.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateRoomRequest (
    @NotBlank(message = "Name must not be blank.") 
    String name,

    @Min(value = 1, message = "Capacity must be at least one.")
    int capacity,

    @NotNull(message = "course restriction must be specified")
    Long restrictedToCourseId
) {

}
