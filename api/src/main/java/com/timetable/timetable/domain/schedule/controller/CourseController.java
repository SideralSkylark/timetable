package com.timetable.timetable.domain.schedule.controller;

import com.timetable.timetable.domain.schedule.dto.CourseRespose;
import com.timetable.timetable.domain.schedule.dto.CreateCourseRequest;
import com.timetable.timetable.domain.schedule.dto.RoomResponse;
import com.timetable.timetable.domain.schedule.dto.UpdateCourseRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateRoomRequest;
import com.timetable.timetable.domain.schedule.service.CourseService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("api/v1/courses")
public class CourseController {
    private final CourseService courseService;

    @PostMapping
    public CourseRespose create(@Valid @RequestBody CreateCourseRequest request) {
        return courseService.createCourse(request);
    }

    @GetMapping
    public Page<CourseRespose> getAll(Pageable pageable) {
        return courseService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public CourseRespose getById(@PathVariable Long id) {
        return courseService.getById(id);
    }

    @PutMapping("/{id}")
    public CourseRespose update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request) {
        return courseService.updateCourse(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }
}
