package com.timetable.timetable.timefold.domain;

import lombok.*;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class TfRoom {
    private Long id;
    private String name;
    private int capacity;
}
