package com.timetable.timetable.domain.user.entity;

public enum UserRole {
    USER(1),
    COORDINATOR(2),
    ADMIN(3);

    private final int priority;

    UserRole(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}

