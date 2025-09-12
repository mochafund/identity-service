package com.mochafund.identityservice.workspace.membership.repository;

import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IMembershipRepository extends JpaRepository<WorkspaceMembership, UUID> {
    List<WorkspaceMembership> findAllByUser_Id(UUID userId);
    long countByUserId(UUID userId);
    void deleteByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);
}
