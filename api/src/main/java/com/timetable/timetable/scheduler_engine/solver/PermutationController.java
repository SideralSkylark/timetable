package com.timetable.timetable.scheduler_engine.solver;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for manual lesson permutation.
 *
 * Flow:
 * 1. POST /valid-slots → frontend highlights valid target cells
 * 2. POST /apply → user confirms, swap is persisted
 *
 * Permutation is only available when viewing a single cohort.
 * The frontend enforces this — the button is disabled otherwise.
 */
@RestController
@RequestMapping("/api/v1/permutations")
@RequiredArgsConstructor
public class PermutationController {

    private final PermutationService permutationService;

    /**
     * Returns timeslots the lesson can be moved to without violating hard
     * constraints.
     *
     * Body: { "scheduledClassId": 42, "academicYear": 2026, "semester": 1 }
     */
    @PostMapping("/valid-slots")
    public ResponseEntity<List<PermutationService.ValidSlotResponse>> getValidSlots(
            @RequestBody ValidSlotsRequest request) {

        List<PermutationService.ValidSlotResponse> slots = permutationService.findValidSlots(
                request.scheduledClassId(),
                request.academicYear(),
                request.semester());

        return ResponseEntity.ok(slots);
    }

    /**
     * Applies a confirmed swap to the persisted timetable.
     *
     * Body: { "scheduledClassId": 42, "targetTimeslotId": 7 }
     */
    @PostMapping("/apply")
    public ResponseEntity<Void> applySwap(@RequestBody ApplySwapRequest request) {
        permutationService.applySwap(request.scheduledClassId(), request.targetTimeslotId());
        return ResponseEntity.noContent().build();
    }

    record ValidSlotsRequest(Long scheduledClassId, int academicYear, int semester) {
    }

    record ApplySwapRequest(Long scheduledClassId, Long targetTimeslotId) {
    }
}
