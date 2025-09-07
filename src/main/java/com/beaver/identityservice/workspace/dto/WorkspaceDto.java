package com.beaver.identityservice.workspace.dto;

import com.beaver.identityservice.common.dto.BaseDto;
import com.beaver.identityservice.workspace.entity.Workspace;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class WorkspaceDto extends BaseDto {
    private String name;
    private String status;
    private String plan;
    private LocalDateTime trialEndsAt;

    public static WorkspaceDto fromEntity(Workspace workspace) {
        return WorkspaceDto.builder()
                .id(workspace.getId())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .name(workspace.getName())
                .status(workspace.getStatus().name())
                .plan(workspace.getPlan().name())
                .trialEndsAt(workspace.getTrialEndsAt())
                .build();
    }
}
