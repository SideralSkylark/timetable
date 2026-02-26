package com.timetable.timetable.domain.schedule.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ConfirmCohortRequest(
    @NotNull
    @Min(value = 1, message = "Número de alunos deve ser positivo")
    @Max(value = 500, message = "Número de alunos demasiado elevado")
    int studentCount
) {}
