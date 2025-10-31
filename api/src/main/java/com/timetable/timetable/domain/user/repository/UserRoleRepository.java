package com.timetable.timetable.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.timetable.timetable.domain.user.entity.UserRole;
import com.timetable.timetable.domain.user.entity.UserRoleEntity;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {
    Optional<UserRoleEntity> findByRole(UserRole role);
}
