package com.timetable.timetable.domain.schedule.controller;

import com.timetable.timetable.common.response.ApiResponse;
import com.timetable.timetable.common.response.ResponseFactory;
import com.timetable.timetable.domain.schedule.dto.CreateTimeSlotRequest;
import com.timetable.timetable.domain.schedule.dto.TimeSlotResponse;
import com.timetable.timetable.domain.schedule.dto.UpdateTimeSlotRequest;
import com.timetable.timetable.domain.schedule.service.TimeSlotService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/timeslots")
public class TimeSlotController {
    private final TimeSlotService timeSlotService;

    @PostMapping
    public ResponseEntity<ApiResponse<TimeSlotResponse>> create(@Valid @RequestBody CreateTimeSlotRequest request) {
        return ResponseFactory.ok(
            TimeSlotResponse.from(timeSlotService.createTimeSlot(request)),
            "Time slot created successfully."
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<TimeSlotResponse>>> getAll(Pageable pageable) {
        return ResponseFactory.ok(
            new PagedModel<>(timeSlotService.getAll(pageable).map(TimeSlotResponse::from)),
            "Time slots fetched successfully."
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> getById(@PathVariable Long id) {
        return ResponseFactory.ok(
            TimeSlotResponse.from(timeSlotService.getById(id)),
            "Time slot fetched successfully."
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTimeSlotRequest request) {
        return ResponseFactory.ok(
            
            TimeSlotResponse.from(timeSlotService.updateTimeSlot(id, request)),
            "Time slot updated successfully."
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeSlotService.deleteTimeSlot(id);
        return ResponseEntity.noContent().build();
    }
}



