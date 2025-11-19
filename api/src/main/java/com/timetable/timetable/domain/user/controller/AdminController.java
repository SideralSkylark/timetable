package com.timetable.timetable.domain.user.controller;

import com.timetable.timetable.auth.dto.RegisterRequestDTO;
import com.timetable.timetable.common.response.ApiResponse;
import com.timetable.timetable.common.response.ResponseFactory;
import com.timetable.timetable.domain.user.dto.UserResponseDTO;
import com.timetable.timetable.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/admins")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(
        @Valid 
        @RequestBody RegisterRequestDTO registerRequest) {
        return ResponseFactory.ok(
            userService.createUser(registerRequest),
            "User created sucessfully."
        );
    }

    @GetMapping
    public void getUsers() {
    }

    @GetMapping("/{id}")
    public void getUserById() {

    }

    @PutMapping("/{id}")
    public void updateUser() {

    }

    @DeleteMapping("/{id}")
    public void deleteUser() {

    }
}
