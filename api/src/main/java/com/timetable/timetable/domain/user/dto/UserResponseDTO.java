package com.timetable.timetable.domain.user.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.timetable.timetable.domain.user.entity.ApplicationUser;
import com.timetable.timetable.domain.user.entity.UserRoleEntity;

public record UserResponseDTO(
	Long id,
    String username,
    String email,
    Set<String> roles,
    boolean enabled
) {
    public static UserResponseDTO from (ApplicationUser user) {
        return new UserResponseDTO(
            user.getId(), 
            user.getUsername(), 
            user.getEmail(), 
            user.getRoles().stream()
                .map(UserRoleEntity::getRole) 
                .map(Enum::name)
                .collect(Collectors.toSet()),
            user.isEnabled());
    }
}
