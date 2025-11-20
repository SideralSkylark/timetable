package com.timetable.timetable.domain.user.controller;

import com.timetable.timetable.common.response.ApiResponse;
import com.timetable.timetable.common.response.ResponseFactory;
import com.timetable.timetable.domain.user.dto.AdminUpdateUserDTO;
import com.timetable.timetable.domain.user.dto.CreateUser;
import com.timetable.timetable.domain.user.dto.UserResponseDTO;
import com.timetable.timetable.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
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
        @RequestBody CreateUser createUser) {
        return ResponseFactory.ok(
            userService.createUser(createUser),
            "User created sucessfully."
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<UserResponseDTO>>> getUsers(Pageable pageable) {
        return ResponseFactory.ok(
            new PagedModel<>(userService.getAllUsers(pageable)),
            "Users fetched sucessfully."
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long id) {
        return ResponseFactory.ok(
            userService.findUserById(id),
            "User fetched sucessfully."
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
        @PathVariable Long id,
        @RequestBody AdminUpdateUserDTO updateRequest
    ) {
        return ResponseFactory.ok(
            userService.updateUserById(id, updateRequest),
            "User updated sucessfully."
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
