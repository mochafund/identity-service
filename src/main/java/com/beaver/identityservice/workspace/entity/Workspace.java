package com.beaver.identityservice.workspace.entity;

import com.beaver.identityservice.common.entity.BaseEntity;
import com.beaver.identityservice.membership.entity.WorkspaceMembership;
import com.beaver.identityservice.role.entity.WorkspaceRole;
import com.beaver.identityservice.workspace.enums.PlanType;
import com.beaver.identityservice.workspace.enums.WorkspaceStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@DynamicUpdate
@Entity
@Table(name = "workspaces")
public class Workspace extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceStatus status = WorkspaceStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType plan = PlanType.STARTER;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkspaceRole> roles = new ArrayList<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkspaceMembership> memberships = new ArrayList<>();
}
