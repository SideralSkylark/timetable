package com.timetable.timetable.domain.schedule.controller;

import com.timetable.timetable.common.response.ApiResponse;
import com.timetable.timetable.common.response.ResponseFactory;
import com.timetable.timetable.domain.schedule.dto.CreateRoomRequest;
import com.timetable.timetable.domain.schedule.dto.RoomResponse;
import com.timetable.timetable.domain.schedule.dto.UpdateRoomRequest;
import com.timetable.timetable.domain.schedule.service.RoomService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("api/v1/rooms")
public class RoomController {
    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> create(@Valid @RequestBody CreateRoomRequest request) {
        return ResponseFactory.ok(
            roomService.createRoom(request),
            "Room created successfully."
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RoomResponse>>> getAll(Pageable pageable) {
        return ResponseFactory.ok(
            roomService.getAll(pageable),
            "Rooms fetched successfully."
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> getById(@PathVariable Long id) {
        return ResponseFactory.ok(
            roomService.getById(id),
            "Room fetched successfully."
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomRequest request) {
        return ResponseFactory.ok(
            roomService.updateRoom(id, request),
            "Room updated successfully."
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
