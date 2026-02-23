package com.timetable.timetable.scheduler_engine.solver;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permutations")
@RequiredArgsConstructor
public class PermutationController {

    private final PermutationService permutationService;

    @PostMapping("/valid-slots")
    public ResponseEntity<List<PermutationService.ValidSlotResponse>> getValidSlots(
            @RequestBody ValidSlotsRequest request) {

        return ResponseEntity.ok(permutationService.findValidSlots(
            request.scheduledClassId(),
            request.academicYear(),
            request.semester()
        ));
    }

    @PostMapping("/apply")
    public ResponseEntity<Void> applySwap(@RequestBody ApplySwapRequest request) {
        permutationService.applySwap(
            request.scheduledClassId(),
            request.targetTimeslotId(),
            request.targetRoomId()
        );
        return ResponseEntity.noContent().build();
    }

    record ValidSlotsRequest(Long scheduledClassId, int academicYear, int semester) {}
    record ApplySwapRequest(Long scheduledClassId, Long targetTimeslotId, Long targetRoomId) {}
}
