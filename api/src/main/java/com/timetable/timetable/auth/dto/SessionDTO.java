package com.timetable.timetable.auth.dto;

import java.time.LocalDateTime;

public record SessionDTO(
    Long tokenId,
    String ip,
    String device,
    LocalDateTime loginTime,
    boolean active
) {}
