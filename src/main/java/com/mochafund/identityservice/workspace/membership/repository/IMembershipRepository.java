package com.mochafund.identityservice.workspace.membership.repository;

import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.mochafund.identityservice.workspace.membership.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
            "WHERE wm.user.id = :userId AND wm.workspace.id = :workspaceId AND wm.status = :status")
    Optional<WorkspaceMembership> findByUserIdAndWorkspaceIdAndStatus(
            @Param("userId") UUID userId,
            @Param("workspaceId") UUID workspaceId,
            @Param("status") MembershipStatus status);

    @Query("SELECT wm FROM WorkspaceMembership wm " +
            "JOIN FETCH wm.workspace " +
            "WHERE wm.user.id = :userId")
    List<WorkspaceMembership> findAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(wm) FROM WorkspaceMembership wm WHERE wm.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM WorkspaceMembership wm WHERE wm.user.id = :userId AND wm.workspace.id = :workspaceId")
    int deleteByUserIdAndWorkspaceId(@Param("userId") UUID userId, @Param("workspaceId") UUID workspaceId);
}
