package com.mochafund.identityservice.workspace.membership.service;

import com.mochafund.identityservice.common.exception.BadRequestException;
import com.mochafund.identityservice.common.exception.ConflictException;
import com.mochafund.identityservice.common.exception.ResourceNotFoundException;
import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.user.repository.IUserRepository;
import com.mochafund.identityservice.workspace.membership.dto.MembershipManagementDto;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.mochafund.identityservice.workspace.membership.enums.MembershipStatus;
import com.mochafund.identityservice.workspace.membership.repository.IMembershipRepository;
import com.mochafund.identityservice.workspace.repository.IWorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class MembershipService implements IMembershipService {

    private final IMembershipRepository membershipRepository;
    private final IWorkspaceRepository workspaceRepository;
    private final IUserRepository userRepository;

    @Transactional(readOnly = true)
    public List<WorkspaceMembership> listAllUserMemberships(UUID userId) {
        return membershipRepository.findAllByUser_Id(userId);
    }

    @Transactional
    public WorkspaceMembership createMembership(UUID userId, UUID workspaceId, Set<Role> roles) {
        log.info("Adding user {} to workspace {} with roles {}", userId, workspaceId, roles);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (membershipRepository.existsByUser_IdAndWorkspace_Id(userId, workspaceId)) {
            throw new ConflictException("User already has a membership to workspace");
        }

        WorkspaceMembership membership = WorkspaceMembership.builder()
                .user(user)
                .workspace(workspace)
                .roles(roles)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        return membershipRepository.save(membership);
    }

    @Transactional
    public WorkspaceMembership updateMembership(UUID userId, UUID workspaceId, MembershipManagementDto membershipDto) {
        log.info("Updating membership with ID: {}", workspaceId);

        WorkspaceMembership membership = membershipRepository
                .findByUser_IdAndWorkspace_Id(userId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not have a membership to workspace"));
        membership.patchFrom(membershipDto);

        return membershipRepository.save(membership);
    }

    @Transactional
    public void deleteMembership(UUID userId, UUID workspaceId) {
        membershipRepository.findByUser_IdAndWorkspace_Id(userId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not have a membership to workspace"));

        long total = membershipRepository.countByUser_Id(userId);
        if (total <= 1) {
            throw new BadRequestException("User can't be removed from their only workspace");
        }

        membershipRepository.deleteByUser_IdAndWorkspace_Id(userId, workspaceId);
        // TODO: Publish workspace.membership.deleted event to Kafka
    }
}
