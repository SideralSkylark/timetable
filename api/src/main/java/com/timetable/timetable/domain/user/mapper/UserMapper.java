package com.timetable.timetable.domain.user.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.timetable.timetable.domain.user.dto.UserResponseDTO;
import com.timetable.timetable.domain.user.entity.ApplicationUser;


@Component
public class UserMapper {
    public UserResponseDTO toDTO(ApplicationUser user) {
        Set<String> roles = user.getRoles().stream()
                                .map(r -> r.getRole().name())
                                .collect(Collectors.toSet());

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles,
                user.isEnabled());
    }
}
