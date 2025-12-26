package com.timetable.timetable.scheduler_engine.solver;

import com.timetable.timetable.scheduler_engine.domain.TimetableSolution;
import com.timetable.timetable.scheduler_engine.mapper.LessonMapper;
import com.timetable.timetable.scheduler_engine.dto.LessonDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/solver")
@RequiredArgsConstructor
public class TimetableSolverController {

    private final TimetableSolverService solverService;
    private final LessonMapper lessonMapper;

    // Endpoint mínimo: recebe JSON da timetable inicial e devolve solução
    @PostMapping
    public List<LessonDTO> solve(@RequestBody TimetableSolution problem) {
        TimetableSolution solution = solverService.solve(problem);

        return solution.getLessons().stream()
                .map(lessonMapper::toDto)
                .collect(Collectors.toList());
    }
}
