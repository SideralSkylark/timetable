package com.timetable.timetable.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailVerificationDTO(
	@NotBlank(message = "Email is required")
	@Email
	String email,

	@NotBlank(message = "Verification code is required")
	@Size(min = 6, max = 6, message = "Verification code must be exactly 6 characters")
	String code
) {}
