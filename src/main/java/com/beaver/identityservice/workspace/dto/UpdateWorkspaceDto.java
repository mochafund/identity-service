package com.beaver.identityservice.workspace.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkspaceDto {

    @Size(min = 1, max = 100, message = "Workspace name must be between 1 and 100 characters")
    private String name;
}
