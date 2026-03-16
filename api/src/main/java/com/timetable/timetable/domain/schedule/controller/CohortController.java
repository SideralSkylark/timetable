package com.timetable.timetable.domain.schedule.controller;

import com.timetable.timetable.common.response.ApiResponse;
import com.timetable.timetable.common.response.ResponseFactory;
import com.timetable.timetable.domain.schedule.dto.CohortListResponse;
import com.timetable.timetable.domain.schedule.dto.CohortResponse;
import com.timetable.timetable.domain.schedule.dto.ConfirmCohortRequest;
import com.timetable.timetable.domain.schedule.dto.CreateCohortRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateCohortRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateCohortStudentsRequest;
import com.timetable.timetable.domain.schedule.query.CohortQueryService;
import com.timetable.timetable.domain.schedule.service.CohortService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("api/v1/cohorts")
public class CohortController {
    private final CohortService cohortService;
    private final CohortQueryService cohortQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CohortResponse>> create(@Valid @RequestBody CreateCohortRequest request) {
        return ResponseFactory.ok(
                CohortResponse.from(cohortService.createCohort(request)),
                "Cohort created successfully.");
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<CohortResponse>> confirm(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmCohortRequest request) {
        return ResponseFactory.ok(
                CohortResponse.from(cohortService.confirmCohort(id, request.studentCount())),
                "Ingressos confirmados com sucesso.");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<CohortListResponse>>> getAll(Pageable pageable) {
        return ResponseFactory.ok(
                new PagedModel<>(cohortQueryService.findAll(pageable)),
                "Cohorts fetched successfully.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CohortResponse>> getById(@PathVariable Long id) {
        return ResponseFactory.ok(
                CohortResponse.from(cohortService.getById(id)),
                "Cohort fetched successfully.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CohortResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCohortRequest request) {
        return ResponseFactory.ok(
                CohortResponse.from(cohortService.updateCohort(id, request)),
                "Cohort updated successfully.");
    }

    @PutMapping("/{id}/students")
    public ResponseEntity<ApiResponse<CohortResponse>> updateStudents(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCohortStudentsRequest request) {
        return ResponseFactory.ok(
                CohortResponse.from(cohortService.updateStudents(id, request.studentIds())),
                "Students updated successfully.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cohortService.deleteCohort(id);
        return ResponseEntity.noContent().build();
    }
}
