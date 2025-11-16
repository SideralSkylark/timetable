package com.timetable.timetable.domain.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.timetable.timetable.domain.user.dto.UpdateUserProfileDTO;
import com.timetable.timetable.domain.user.dto.UserResponseDTO;
import com.timetable.timetable.domain.user.entity.ApplicationUser;
import com.timetable.timetable.domain.user.entity.UserRole;
import com.timetable.timetable.domain.user.exception.UserNotFoundException;
import com.timetable.timetable.domain.user.mapper.UserMapper;
import com.timetable.timetable.domain.user.repository.UserRepository;
import com.timetable.timetable.security.SecurityUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserResponseDTO getAuthenticatedUserProfile() {
        String username = SecurityUtil.getAuthenticatedUsername();
        ApplicationUser user = getUser(username);
        return userMapper.toDTO(user);
    }

    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDTO);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toDTO).toList();
    }

    public List<UserResponseDTO> getUsersByRole(UserRole role) {
        return userRepository.findAllByRole(role).stream().map(userMapper::toDTO).toList();
    }

    public Page<UserResponseDTO> getUsersByRole(UserRole role, Pageable pageable) {
        return userRepository.findAllByRole(role, pageable).map(userMapper::toDTO);
    }

    public Optional<ApplicationUser> findById(Long id) {
        return userRepository.findById(id);
    }

    public ApplicationUser getUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User %d not found".formatted(id)));
    }

    public Optional<ApplicationUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<ApplicationUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

	public UserResponseDTO updateAuthenticatedUserProfile (UpdateUserProfileDTO dto) {
		String username = SecurityUtil.getAuthenticatedUsername();

		ApplicationUser updated = updateUser(username, dto);

		return userMapper.toDTO(updated);
	}

    @Transactional
    public ApplicationUser updateUser(String username, UpdateUserProfileDTO dto) {
        ApplicationUser user = getUser(username);

        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setUpdatedAt(LocalDateTime.now());

        ApplicationUser updated = userRepository.save(user);
        log.info("User '{}' updated successfully", username);
        return updated;
    }

	public void deleteAuthenticatedUserProfile() {
		String username = SecurityUtil.getAuthenticatedUsername();
		deleteByUsername(username);
	}

    @Transactional
    public void deleteByUsername(String username) {
        ApplicationUser user = getUser(username);
        if (user.isDeleted()) {
            log.warn("User '{}' is already deleted.", username);
            return;
        }

        Long deleterId = SecurityUtil.getAuthenticatedId();
        user.markAsDeleted(deleterId);
        userRepository.save(user);

        log.info("User '{}' marked as deleted by {}", username, deleterId);
    }

    public boolean enableAccount(String email) {
        return updateAccountStatus(email, true);
    }

    @Transactional
    public boolean disableAccount(String email) {
        return updateAccountStatus(email, false);
    }

    private boolean updateAccountStatus(String email, boolean enable) {
        ApplicationUser user = getUserByEmailOrThrow(email);
        boolean alreadyEnabled = user.isEnabled();

        if (enable && alreadyEnabled) return false;
        if (!enable && !alreadyEnabled) return false;

        if (enable) user.activate();
        else user.deactivate();

        userRepository.save(user);
        log.info("Account '{}' set to {}", email, enable ? "ENABLED" : "DISABLED");
        return true;
    }

    private ApplicationUser getUser(String username) {
        return getOrThrow(username, userRepository::findByUsername, "No user found with username: " + username);
    }

    private ApplicationUser getUserByEmailOrThrow(String email) {
        return getOrThrow(email, userRepository::findByEmail, "No user found with email: " + email);
    }

    private <T> ApplicationUser getOrThrow(
            T value,
            Function<T, Optional<ApplicationUser>> finder,
            String errorMessage
    ) {
        return finder.apply(value)
                .orElseThrow(() -> new UserNotFoundException(errorMessage));
    }
}

