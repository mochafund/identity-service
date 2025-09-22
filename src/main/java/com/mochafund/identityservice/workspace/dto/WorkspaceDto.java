package com.mochafund.identityservice.workspace.dto;

import com.mochafund.identityservice.common.dto.BaseDto;
import com.mochafund.identityservice.workspace.entity.Workspace;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class WorkspaceDto extends BaseDto {
    private String name;
    private String status;

    public static WorkspaceDto fromEntity(Workspace workspace) {
        return WorkspaceDto.builder()
                .id(workspace.getId())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .name(workspace.getName())
                .status(workspace.getStatus().name())
                .build();
    }

    public static List<WorkspaceDto> fromEntities(List<Workspace> workspaces) {
        return workspaces.stream().map(WorkspaceDto::fromEntity).toList();
    }
}
