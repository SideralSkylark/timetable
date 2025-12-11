package com.timetable.timetable.domain.schedule.controller;

import com.timetable.timetable.common.response.ApiResponse;
import com.timetable.timetable.common.response.ResponseFactory;
import com.timetable.timetable.domain.schedule.dto.CohortResponse;
import com.timetable.timetable.domain.schedule.dto.CreateCohortRequest;
import com.timetable.timetable.domain.schedule.dto.UpdateCohortRequest;
import com.timetable.timetable.domain.schedule.service.CohortService;

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
@RequestMapping("api/v1/cohorts")
public class CohortController {
    private final CohortService cohortService;

    @PostMapping
    public ResponseEntity<ApiResponse<CohortResponse>> create(@Valid @RequestBody CreateCohortRequest request) {
        return ResponseFactory.ok(
            CohortResponse.from(cohortService.createCohort(request)),
            "Cohort created successfully."
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<CohortResponse>>> getAll(Pageable pageable) {
        return ResponseFactory.ok(
            new PagedModel<>(cohortService.getAll(pageable).map(CohortResponse::from)),
            "Cohorts fetched successfully."
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CohortResponse>> getById(@PathVariable Long id) {
        return ResponseFactory.ok(
            CohortResponse.from(cohortService.getById(id)),
            "Cohort fetched successfully."
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CohortResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCohortRequest request) {
        return ResponseFactory.ok(
            CohortResponse.from(cohortService.updateCohort(id, request)),
            "Cohort updated successfully."
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cohortService.deleteCohort(id);
        return ResponseEntity.noContent().build();
    }
}

