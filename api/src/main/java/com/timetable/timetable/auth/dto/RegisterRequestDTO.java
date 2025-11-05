package com.timetable.timetable.auth.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
	@NotBlank(message = "Username is required")
	String username,

	@Email
	@NotBlank(message = "Email is required")
	String email,


    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
	String password,


    @Size(min = 1, max = 3)
    List<@NotBlank(message = "Role must not be blank") String> roles


    // @Pattern(regexp = "^(ACTIVE|INACTIVE)?$",
            //  message = "status must be ACTIVE or INACTIVE")
    // String status
) {}
