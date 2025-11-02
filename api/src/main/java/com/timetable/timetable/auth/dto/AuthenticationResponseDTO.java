package com.timetable.timetable.auth.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record AuthenticationResponseDTO(
	Long id,
	String username,
	String email,
	Set<String> roles,
	LocalDateTime updatedAt
){}

