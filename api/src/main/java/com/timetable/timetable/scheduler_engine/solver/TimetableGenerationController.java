// package com.timetable.timetable.scheduler_engine.solver;
//
// import com.timetable.timetable.domain.schedule.entity.Timetable;
// import com.timetable.timetable.domain.schedule.entity.TimetableStatus;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
//
// /**
//  * REST API for production timetable generation.
//  * 
//  * This controller generates timetables using data from the database
//  * and persists the results back to the database.
//  * 
//  * Endpoints:
//  * - POST /api/v1/timetable/generate - Generate new timetable from DB data
//  * - POST /api/v1/timetable/{id}/regenerate - Regenerate existing timetable
//  * - GET  /api/v1/timetable/{id} - Get timetable details
//  */
// @RestController
// @Slf4j
// @RequestMapping("/api/v1/timetable")
// @RequiredArgsConstructor
// public class TimetableGenerationController {
//
//     private final TimetableGenerationService generationService;
//
//     /**
//      * Generate a new timetable for a specific academic period.
//      * 
//      * This endpoint:
//      * 1. Fetches all active CohortSubjects for the period from DB
//      * 2. Runs the solver to allocate timeslots and rooms
//      * 3. Saves the result as a new Timetable with ScheduledClasses
//      * 
//      * Example request:
//      * POST /api/v1/timetable/generate
//      * {
//      *   "academicYear": 2026,
//      *   "semester": 1,
//      *   "solverTimeoutSeconds": 60
//      * }
//      * 
//      * @param request The generation request
//      * @return The generated timetable with metadata
//      */
//     @PostMapping("/generate")
//     public ResponseEntity<TimetableGenerationResponse> generateTimetable(
//             @RequestBody GenerateTimetableRequest request) {
//
//         log.info("Received request to generate timetable for {}.{} (timeout: {}s)",
//             request.academicYear(), request.semester(), request.solverTimeoutSeconds());
//
//         try {
//             Timetable timetable = generationService.generateTimetable(
//                 request.academicYear(),
//                 request.semester(),
//                 request.solverTimeoutSeconds() != null ? request.solverTimeoutSeconds() : 60L
//             );
//
//             return ResponseEntity.ok(new TimetableGenerationResponse(
//                 timetable.getId(),
//                 timetable.getAcademicYear(),
//                 timetable.getSemester(),
//                 timetable.getStatus(),
//                 timetable.getScheduledClasses() != null ? timetable.getScheduledClasses().size() : 0,
//                 "Timetable generated successfully",
//                 true
//             ));
//
//         } catch (IllegalStateException e) {
//             log.error("Failed to generate timetable: {}", e.getMessage());
//             return ResponseEntity.unprocessableEntity()
//                 .body(new TimetableGenerationResponse(
//                     null,
//                     request.academicYear(),
//                     request.semester(),
//                     null,
//                     0,
//                     "Failed: " + e.getMessage(),
//                     false
//                 ));
//         }
//     }
//
//     /**
//      * Regenerate an existing timetable (deletes old schedule and creates new one).
//      * 
//      * This is useful when:
//      * - You want to try different solver configurations
//      * - Input data (cohort-subjects, timeslots, rooms) has changed
//      * - The previous solution was not satisfactory
//      * 
//      * Example request:
//      * POST /api/v1/timetable/123/regenerate
//      * {
//      *   "solverTimeoutSeconds": 120
//      * }
//      * 
//      * @param timetableId The ID of the timetable to regenerate
//      * @param request The regeneration request
//      * @return The newly generated timetable
//      */
//     @PostMapping("/{timetableId}/regenerate")
//     public ResponseEntity<TimetableGenerationResponse> regenerateTimetable(
//             @PathVariable Long timetableId,
//             @RequestBody RegenerateTimetableRequest request) {
//
//         log.info("Received request to regenerate timetable #{} (timeout: {}s)",
//             timetableId, request.solverTimeoutSeconds());
//
//         try {
//             Timetable timetable = generationService.regenerateTimetable(
//                 timetableId,
//                 request.solverTimeoutSeconds() != null ? request.solverTimeoutSeconds() : 60L
//             );
//
//             return ResponseEntity.ok(new TimetableGenerationResponse(
//                 timetable.getId(),
//                 timetable.getAcademicYear(),
//                 timetable.getSemester(),
//                 timetable.getStatus(),
//                 timetable.getScheduledClasses() != null ? timetable.getScheduledClasses().size() : 0,
//                 "Timetable regenerated successfully",
//                 true
//             ));
//
//         } catch (IllegalArgumentException e) {
//             log.error("Timetable not found: {}", e.getMessage());
//             return ResponseEntity.notFound().build();
//
//         } catch (IllegalStateException e) {
//             log.error("Failed to regenerate timetable: {}", e.getMessage());
//             return ResponseEntity.unprocessableEntity()
//                 .body(new TimetableGenerationResponse(
//                     timetableId,
//                     null,
//                     null,
//                     null,
//                     0,
//                     "Failed: " + e.getMessage(),
//                     false
//                 ));
//         }
//     }
//
//     /**
//      * Quick generation with default settings (60s timeout).
//      * 
//      * Example:
//      * POST /api/v1/timetable/generate/quick?year=2026&semester=1
//      * 
//      * @param academicYear The academic year
//      * @param semester The semester (1 or 2)
//      * @return The generated timetable
//      */
//     @PostMapping("/generate/quick")
//     public ResponseEntity<TimetableGenerationResponse> generateQuick(
//             @RequestParam int academicYear,
//             @RequestParam int semester) {
//
//         return generateTimetable(new GenerateTimetableRequest(academicYear, semester, 60L));
//     }
//
//     // ========================================
//     // REQUEST/RESPONSE DTOs
//     // ========================================
//
//     record GenerateTimetableRequest(
//         int academicYear,
//         int semester,
//         Long solverTimeoutSeconds // Optional, defaults to 60
//     ) {}
//
//     record RegenerateTimetableRequest(
//         Long solverTimeoutSeconds // Optional, defaults to 60
//     ) {}
//
//     record TimetableGenerationResponse(
//         Long timetableId,
//         Integer academicYear,
//         Integer semester,
//         TimetableStatus status,
//         int scheduledClassesCount,
//         String message,
//         boolean success
//     ) {}
// }
