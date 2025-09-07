package com.beaver.identityservice.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SwitchWorkspaceDto {

    @NotBlank(message = "Workspace ID must be provided")
    @org.hibernate.validator.constraints.UUID
    private UUID workspaceId;
}
