package com.timetable.timetable.domain.user.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SoftDelete(strategy = SoftDeleteType.ACTIVE)
@Table(name = "users")
@ToString(exclude = "password")
public class ApplicationUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.INACTIVE;

    @Builder.Default
    private boolean deleted = false;

    private Instant deletedAt;

    private Long deletedByUserId;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<UserRoleEntity> roles = new HashSet<>();

    // ====== DOMAIN LOGIC ======

    public boolean addRole(UserRoleEntity role) {
        return roles.add(role);
    }

    public boolean removeRole(UserRoleEntity role) {
        return roles.remove(role);
    }

    public boolean hasRole(UserRole role) {
        return roles.stream().anyMatch(r -> r.getRole() == role);
    }

    public boolean lacksRole(UserRole role) {
        return roles.stream().noneMatch(r -> r.getRole() == role);
    }

    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    public boolean isUser() {
        return roles.size() == 1 && hasRole(UserRole.USER);
    }

    public UserRole getHighestPrivilegeRole() {
        return roles.stream()
                .map(UserRoleEntity::getRole)
                .max(Comparator.comparingInt(UserRole::getPriority))
                .orElse(UserRole.USER);
    }

    public void activate() {
        this.status = AccountStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = AccountStatus.INACTIVE;
    }

    public void markAsDeleted(Long deletedByUserId) {
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.deletedByUserId = deletedByUserId;
        this.status = AccountStatus.INACTIVE;
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedByUserId = null;
    }

    // ====== SPRING SECURITY ======

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> (GrantedAuthority) () -> "ROLE_" + role.getRole().name())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return !deleted;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !deleted;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == AccountStatus.ACTIVE && !deleted;
    }

    // ====== FACTORY ======

    public static ApplicationUser createUser(String username, String email, String password) {
        return ApplicationUser.builder()
                .username(username)
                .email(email)
                .password(password)
                .status(AccountStatus.INACTIVE)
                .deleted(false)
                .roles(new HashSet<>())
                .build();
    }

    // ====== EQUALITY ======

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationUser user)) return false;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}

