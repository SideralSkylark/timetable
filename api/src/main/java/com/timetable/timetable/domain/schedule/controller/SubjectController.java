package com.timetable.timetable.domain.schedule.controller;

import com.timetable.timetable.common.response.ApiResponse;
import com.timetable.timetable.common.response.ResponseFactory;
import com.timetable.timetable.domain.schedule.dto.CreateSubjectRequest;
import com.timetable.timetable.domain.schedule.dto.SubjectListResponse;
import com.timetable.timetable.domain.schedule.dto.UpdateSubjectRequest;
import com.timetable.timetable.domain.schedule.service.SubjectService;

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
@RequestMapping("api/v1/subjects")
public class SubjectController {
    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubjectListResponse>> create(
            @Valid @RequestBody CreateSubjectRequest request) {
        return ResponseFactory.ok(
            SubjectListResponse.from(subjectService.createSubject(request)),
            "subject created"
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectListResponse>> getById(@PathVariable Long id) {
        return ResponseFactory.ok(
            SubjectListResponse.from(subjectService.getById(id)),
            "Subject fetched successfully."
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectListResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request) {
        return ResponseFactory.ok(
            SubjectListResponse.from(subjectService.updateSubject(id, request)),
            "subject updated successfully."
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}


