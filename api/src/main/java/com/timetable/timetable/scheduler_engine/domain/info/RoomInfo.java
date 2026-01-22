package com.timetable.timetable.scheduler_engine.domain.info;

import lombok.*;

/**
 * Represents a physical room where lessons can take place.
 * This is a lightweight, immutable representation for the solver.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomInfo {
    
    /**
     * Unique identifier from the Room entity
     */
    private Long id;
    
    /**
     * Room name/number (e.g., "A101", "Lab 3")
     */
    private String name;
    
    /**
     * Maximum number of students the room can accommodate
     */
    private int capacity;
    
    /**
     * If set, this room can only be used by the specified course.
     * If null, the room is available to all courses.
     */
    private Long restrictedToCourseId;
    
    /**
     * Checks if this room can be used by a specific course
     */
    public boolean isAvailableForCourse(Long courseId) {
        // If no restriction, available to all
        if (restrictedToCourseId == null) {
            return true;
        }
        // Otherwise, only available if course matches
        return restrictedToCourseId.equals(courseId);
    }
    
    /**
     * Checks if this room has sufficient capacity for a given number of students
     */
    public boolean hasSufficientCapacity(int studentCount) {
        return capacity >= studentCount;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomInfo)) return false;
        RoomInfo that = (RoomInfo) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "RoomInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", capacity=" + capacity +
                ", restricted=" + (restrictedToCourseId != null) +
                '}';
    }
}
