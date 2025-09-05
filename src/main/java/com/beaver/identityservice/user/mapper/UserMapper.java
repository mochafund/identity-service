package com.beaver.identityservice.user.mapper;

import com.beaver.identityservice.user.dto.UserDto;
import com.beaver.identityservice.user.entity.User;

public class UserMapper {
    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .isActive(user.getIsActive())
                .lastWorkspaceId(user.getLastWorkspaceId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
