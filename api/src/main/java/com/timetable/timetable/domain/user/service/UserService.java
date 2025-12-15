package com.timetable.timetable.domain.user.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.timetable.timetable.auth.exception.UserAlreadyExistsException;
import com.timetable.timetable.domain.user.dto.*;
import com.timetable.timetable.domain.user.entity.*;
import com.timetable.timetable.domain.user.exception.UserNotFoundException;
import com.timetable.timetable.domain.user.repository.UserRepository;
import com.timetable.timetable.domain.user.repository.UserRoleRepository;
import com.timetable.timetable.security.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ============================================================
    // CREATE USER
    // ============================================================
    @Transactional
    public ApplicationUser createUser(CreateUser request) {
        log.debug("Creating user");
        validateUniqueUser(request);
        Set<UserRoleEntity> roles = resolveRoles(request.roles());

        ApplicationUser user = ApplicationUser.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .roles(roles)
                .build();

        userRepository.save(user);
        log.info("Created new user '{}' with roles {}", user.getUsername(), roles);
        return user;
    }

    // ============================================================
    // READ USERS
    // ============================================================
    public ApplicationUser getAuthenticatedUserProfile() {
        return getByUsernameOrThrow(SecurityUtil.getAuthenticatedUsername());
    }

    public List<ApplicationUser> getAllUsers() {
        return userRepository.findAll();
    }

    public Page<ApplicationUser> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public List<ApplicationUser> getUsersByRole(UserRole role) {
        return userRepository.findAllByRole(role);
    }

    public Page<ApplicationUser> getUsersByRole(UserRole role, Pageable pageable) {
        return userRepository.findAllByRole(role, pageable);
    }

    public ApplicationUser getUserById(Long id) {
        return getByIdOrThrow(id);
    }

    public ApplicationUser getUserByUsername(String username) {
        return getByUsernameOrThrow(username);
    }

    public ApplicationUser getUserByEmail(String email) {
        return getByEmailOrThrow(email);
    }

    // ============================================================
    // UPDATE USERS
    // ============================================================
    @Transactional
    public ApplicationUser updateAuthenticatedUserProfile(UpdateUserProfileDTO dto) {
        log.debug("Updating user profile");
        ApplicationUser user = getByUsernameOrThrow(SecurityUtil.getAuthenticatedUsername());
        updateBasicFields(user, dto.username(), dto.email());
        log.info("User '{}' updated their profile", user.getUsername());
        return userRepository.save(user);
    }

    @Transactional
    public ApplicationUser updateUserById(Long id, AdminUpdateUserDTO payload) {
        log.debug("Updating user {}", id);
        ApplicationUser user = getByIdOrThrow(id);

        validateUniqueUpdate(id, payload.username(), payload.email());

        Set<UserRoleEntity> newRoles = resolveRoles(payload.roles());
        validateAdminRemoval(user, newRoles);

        updateBasicFields(user, payload.username(), payload.email());
        user.setRoles(newRoles);

        userRepository.save(user);
        log.info("Admin updated user '{}' with roles {}", id, newRoles);
        return user;
    }

    // ============================================================
    // DELETE USERS
    // ============================================================
    @Transactional
    public void deleteAuthenticatedUserProfile() {
        deleteByUsername(SecurityUtil.getAuthenticatedUsername());
    }

    @Transactional
    public void deleteByUsername(String username) {
        ApplicationUser user = getByUsernameOrThrow(username);

        userRepository.delete(user);
    }

    @Transactional
    public void deleteById(Long id) {
        ApplicationUser user = getByIdOrThrow(id);

        userRepository.delete(user);
    }

    // ============================================================
    // ACCOUNT STATUS
    // ============================================================
    @Transactional
    public boolean enableAccount(String email) {
        return updateAccountStatus(email, true);
    }

    @Transactional
    public boolean disableAccount(String email) {
        return updateAccountStatus(email, false);
    }

    private boolean updateAccountStatus(String email, boolean enable) {
        ApplicationUser user = getByEmailOrThrow(email);

        if (user.isEnabled() == enable) return false;

        if (enable) user.activate();
        else user.deactivate();

        userRepository.save(user);
        log.info("Account '{}' set to {}", email, enable ? "ENABLED" : "DISABLED");
        return true;
    }

    // ============================================================
    // HELPERS (GETTERS)
    // ============================================================
    private ApplicationUser getByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User %d not found".formatted(id)));
    }

    private ApplicationUser getByUsernameOrThrow(String username) {
        return getOrThrow(username, userRepository::findByUsername, 
                "No user found with username: " + username);
    }

    private ApplicationUser getByEmailOrThrow(String email) {
        return getOrThrow(email, userRepository::findByEmail,
                "No user found with email: " + email);
    }

    private <T> ApplicationUser getOrThrow(T key, Function<T, Optional<ApplicationUser>> finder, String errorMessage) {
        return finder.apply(key)
                .orElseThrow(() -> new UserNotFoundException(errorMessage));
    }

    private void updateBasicFields(ApplicationUser user, String username, String email) {
        user.setUsername(username);
        user.setEmail(email);
        user.setUpdatedAt(LocalDateTime.now());
    }

    // ============================================================
    // VALIDATION HELPERS
    // ============================================================
    private void validateUniqueUser(CreateUser request) {
        validateUsernameAvailable(request.username());
        validateEmailAvailable(request.email());
    }

    private void validateUniqueUpdate(Long id, String username, String email) {
        validateUsernameAvailable(username, id);
        validateEmailAvailable(email, id);
    }

    private void validateUsernameAvailable(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username is already taken");
        }
    }

    private void validateEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email is already in use");
        }
    }

    private void validateUsernameAvailable(String username, Long userId) {
        userRepository.findByUsername(username)
                .filter(u -> !u.getId().equals(userId))
                .ifPresent(u -> { throw new UserAlreadyExistsException("Username is already taken"); });
    }

    private void validateEmailAvailable(String email, Long userId) {
        userRepository.findByEmail(email)
                .filter(u -> !u.getId().equals(userId))
                .ifPresent(u -> { throw new UserAlreadyExistsException("Email is already in use"); });
    }

    private void validateAdminRemoval(ApplicationUser user, Set<UserRoleEntity> newRoles) {
        boolean removingAdmin = user.hasRole(UserRole.ADMIN)
                && newRoles.stream().noneMatch(r -> r.getRole() == UserRole.ADMIN);

        long adminCount = userRepository.countUsersByRole(UserRole.ADMIN);

        if (removingAdmin && adminCount == 1) {
            throw new IllegalArgumentException("Cannot remove ADMIN role from the only admin user.");
        }
    }

    // ============================================================
    // ROLES HELPERS
    // ============================================================
    private Set<UserRoleEntity> resolveRoles(List<String> roleNames) {
        return roleNames.stream()
                .map(roleName -> {
                    UserRole roleEnum;
                    try {
                        roleEnum = UserRole.valueOf(roleName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid role: " + roleName);
                    }

                    return roleRepository.findByRole(roleEnum)
                            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleEnum));
                })
                .collect(Collectors.toSet());
    }
}
