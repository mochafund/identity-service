package com.beaver.identityservice.role.service;

import com.beaver.identityservice.role.entity.WorkspaceRole;
import com.beaver.identityservice.role.enums.Role;
import com.beaver.identityservice.role.repository.IWorkspaceRoleRepository;
import com.beaver.identityservice.workspace.entity.Workspace;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
@CacheConfig(cacheNames = "workspace_roles")
public class WorkspaceRoleService implements IWorkspaceRoleService {

    private final IWorkspaceRoleRepository roleRepository;

    public void createDefaultRoles(UUID workspaceId, Workspace workspace) {
        log.info("Creating default RBAC roles for workspace: {}", workspaceId);

        Set<Role> existingRoles = roleRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(WorkspaceRole::getRoleType)
                .collect(Collectors.toSet());

        List<WorkspaceRole> rolesToCreate = Arrays.stream(Role.values())
                .filter(roleType -> !existingRoles.contains(roleType))
                .map(roleType -> WorkspaceRole.builder()
                        .workspace(workspace)
                        .roleType(roleType)
                        .build())
                .collect(Collectors.toList());

        if (!rolesToCreate.isEmpty()) {
            roleRepository.saveAll(rolesToCreate);
            log.debug("Created {} roles for workspace {}: {}",
                    rolesToCreate.size(), workspaceId,
                    rolesToCreate.stream().map(WorkspaceRole::getRoleType).collect(Collectors.toList()));
        } else {
            log.debug("All roles already exist for workspace {}", workspaceId);
        }
    }

    @Transactional(readOnly = true)
    public Optional<WorkspaceRole> findByWorkspaceIdAndRoleType(UUID workspaceId, Role roleType) {
        return roleRepository.findByWorkspaceIdAndRoleType(workspaceId, roleType);
    }
}