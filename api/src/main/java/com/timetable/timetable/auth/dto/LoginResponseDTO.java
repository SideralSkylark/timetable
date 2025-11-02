package com.timetable.timetable.auth.dto;

public record LoginResponseDTO(
    AuthenticationResponseDTO user,
    String accessToken,
    String refreshToken
) {}

