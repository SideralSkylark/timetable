package com.timetable.timetable.domain.user.entity;

public enum UserRole {
    USER(1),
    STUDENT(2),
    TEACHER(3),
    COORDINATOR(4),
    ADMIN(5);

    private final int priority;

    UserRole(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}

