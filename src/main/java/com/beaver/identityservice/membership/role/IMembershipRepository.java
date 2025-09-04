package com.beaver.identityservice.membership.role;

import com.beaver.identityservice.membership.entity.WorkspaceMembership;
import com.beaver.identityservice.membership.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IMembershipRepository extends JpaRepository<WorkspaceMembership, UUID> {

    @Query("SELECT wm FROM WorkspaceMembership wm " +
            "JOIN FETCH wm.workspace " +
            "JOIN FETCH wm.role " +
            "WHERE wm.user.id = :userId AND wm.status = :status")
    List<WorkspaceMembership> findByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") MembershipStatus status);

    @Query("SELECT wm FROM WorkspaceMembership wm " +
            "JOIN FETCH wm.workspace " +
            "JOIN FETCH wm.role " +
            "WHERE wm.user.id = :userId AND wm.workspace.id = :workspaceId AND wm.status = :status")
    Optional<WorkspaceMembership> findByUserIdAndWorkspaceIdAndStatus(
            @Param("userId") UUID userId,
            @Param("workspaceId") UUID workspaceId,
            @Param("status") MembershipStatus status);
}
