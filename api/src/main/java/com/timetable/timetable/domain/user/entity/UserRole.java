package com.timetable.timetable.domain.user.entity;

public enum UserRole {
    USER(1),
    ADMIN(2);

    private final int priority;

    UserRole(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}

