package com.timetable.timetable.domain.user.dto;

import java.util.Set;

public record UserResponseDTO(
	Long id,
    String username,
    String email,
    Set<String> roles,
    boolean enabled
) {}
