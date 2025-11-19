package com.timetable.timetable.domain.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.timetable.timetable.auth.dto.RegisterRequestDTO;
import com.timetable.timetable.auth.exception.UserAlreadyExistsException;
import com.timetable.timetable.domain.user.dto.UpdateUserProfileDTO;
import com.timetable.timetable.domain.user.dto.UserResponseDTO;
import com.timetable.timetable.domain.user.entity.AccountStatus;
import com.timetable.timetable.domain.user.entity.ApplicationUser;
import com.timetable.timetable.domain.user.entity.UserRole;
import com.timetable.timetable.domain.user.entity.UserRoleEntity;
import com.timetable.timetable.domain.user.exception.UserNotFoundException;
import com.timetable.timetable.domain.user.mapper.UserMapper;
import com.timetable.timetable.domain.user.repository.UserRepository;
import com.timetable.timetable.domain.user.repository.UserRoleRepository;
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
    private final UserRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserResponseDTO createUser(RegisterRequestDTO request) {
        validateUniqueUser(request);
        log.debug("Creating user with username: {}, roles: {}", request.username(), request.roles());
        
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

        return UserResponseDTO.from(user);

    }

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


    /**
     * Validates whether the given username and email are unique.
     *
     * @param request the registration request
     * @throws UserAlreadyExistsException if the username or email is already taken
     */
    private void validateUniqueUser(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.username())) {
            log.warn("Username taken: {}", request.username());
            throw new UserAlreadyExistsException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Email in use: {}", request.email());
            throw new UserAlreadyExistsException("Email is already in use");
        }
    }

    /**
     * Resolves a list of role names (as strings) into corresponding {@link UserRoleEntity} objects.
     *
     * <p>This method:
     * <ul>
     *   <li>Converts each role name to uppercase and maps it to the corresponding {@link UserRole} enum</li>
     *   <li>Fetches the matching {@link UserRoleEntity} from the repository</li>
     *   <li>Throws an {@link IllegalArgumentException} if any role is invalid or not found</li>
     * </ul>
     *
     * @param roleNames A set of role names provided by the user (e.g., "user", "service_provider")
     * @return A set of resolved {@link UserRoleEntity} objects
     * @throws IllegalArgumentException if any role name is not valid or not present in the database
     */
    private Set<UserRoleEntity> resolveRoles(List<String> roleNames) {
        return roleNames.stream()
            .map(role -> roleRepository.findByRole(UserRole.valueOf(role.toUpperCase()))
                .orElseThrow(() -> {
                    log.warn("Invalid role provided: {}", role);
                    return new IllegalArgumentException("Invalid role: " + role);
                }))
            .collect(Collectors.toSet());
    }
}

