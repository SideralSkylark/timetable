package com.timetable.timetable.domain.schedule.dto;

public enum PhantomTeacherPolicy {
    NEVER,            // Falha se não houver professor
    CREATE_IF_NEEDED  // Cria fantasma se necessário
}
