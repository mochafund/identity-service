package com.mochafund.identityservice.user.dto;

import com.mochafund.identityservice.common.dto.BaseDto;
import com.mochafund.identityservice.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class UserDto extends BaseDto {
    private String name;
    private String email;
    private Boolean isActive;
    private UUID lastWorkspaceId;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .name(user.getName())
                .email(user.getEmail())
                .isActive(user.getIsActive())
                .lastWorkspaceId(user.getLastWorkspaceId())
                .build();
    }

    public static List<UserDto> fromEntities(List<User> users) {
        return users.stream().map(UserDto::fromEntity).toList();
    }
}
