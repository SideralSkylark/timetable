package com.timetable.timetable.auth.mapper;

import org.springframework.stereotype.Component;

import com.timetable.timetable.auth.dto.SessionDTO;
import com.timetable.timetable.auth.entity.RefreshToken;

@Component
public class SessionMapper {
    public SessionDTO toSessionDTO(RefreshToken token) {
		SessionDTO dto = new SessionDTO(
			token.getId(), 
			token.getIp(), 
			token.getUserAgent(), 
			token.getCreatedAt(), 
			token.isActive()
		);
		return dto;
	}
}
