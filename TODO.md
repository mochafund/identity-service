# TODO: Workspace/Membership reorg and Keycloak sync cleanup (no events, no pagination)

Purpose
- Reduce coupling between Keycloak adapter and domain services without introducing events.
- Tidy workspace/membership boundaries and simplify APIs while preserving current behavior.

Assumptions
- No domain events or pagination in this iteration.
- Roles are workspace-scoped and persisted as JSONB enum names.
- Prefer additive changes first (new interfaces, wrappers, convenience methods), then migrate callers incrementally.

High-level plan
1) Decouple KeycloakAdminService from membership details via an attribute contribution boundary.
2) Split workspace creation concerns from membership; focus MembershipService on membership operations only.
3) Consolidate membership queries under a flexible query object, keep thin wrappers for common call sites.
4) Replace transport exceptions in services with domain exceptions and add a global exception handler.
5) Clarify role scope and avoid breaking persistence.
6) Improve repository patterns (projections) and strengthen invariants (last membership, owner constraints).

Work items (checklist)

Keycloak integration (no events)
- [ ] Introduce AttributeContributor interface (in a neutral package or keycloak boundary):
      - Contract: Map<String, List<String>> contribute(UUID userId, Optional<UUID> workspaceId)
      - Implement MembershipAttributeContributor that provides: user_id, workspace_id, roles
- [ ] Add KeycloakAttributeAggregator that collects contributions from all AttributeContributor beans.
- [ ] Refactor KeycloakAdminService.syncAttributes to use the aggregator (remove direct IMembershipService usage).
- [ ] Make attribute keys configurable with defaults:
      - keycloak.attributes.userIdKey = user_id
      - keycloak.attributes.workspaceIdKey = workspace_id
      - keycloak.attributes.rolesKey = roles
- [ ] Create KeycloakUserClient wrapper (getUser, updateUser, logout, delete) and adapt KeycloakAdminService to use it.
- [ ] Keep normalize/upsert logic; demote routine "no changes" logs to debug.
- [ ] application.yml: add keycloak.attributes.* with defaults and comments.
- [ ] Wire properties into the AttributeContributor and KeycloakAdminService.

Membership and workspace responsibilities
- [ ] Create WorkspaceService with createDefaultForUser(User user, String requestedName):
      - Unique-name disambiguation per owner ("Name", "Name (2)")
      - Workspace creation defaults (status ACTIVE, plan STARTER)
- [ ] Move createDefaultMembership out of MembershipService:
      - Use WorkspaceService + MembershipService.addUserToWorkspace with OWNER/READ/WRITE
- [ ] Update controllers/use-cases to call WorkspaceService for default workspace creation.

Membership service API simplification
- [ ] Define MembershipQuery (userId, workspaceId, status) in membership package.
- [ ] Add IMembershipService.find(MembershipQuery) and findOne(MembershipQuery).
- [ ] Keep convenience wrappers:
      - getUserMembershipInWorkspace(userId, workspaceId)
      - getAllWorkspaceMemberships(workspaceId)
      - getAllUserMemberships(userId)

Domain exceptions and error handling
- [ ] Add common.exceptions:
      - DomainRuleViolationException
      - ResourceNotFoundException
- [ ] Replace jakarta.ws.rs.NotAllowedException in MembershipService.deleteByUserIdAndWorkspaceId with DomainRuleViolationException.
- [ ] Add @ControllerAdvice to map domain exceptions to HTTP statuses.

Role enum scope (non-breaking now)
- [ ] Keep enum name Role as-is to preserve JSONB values.
- [ ] Add Javadoc clarifying Role is workspace-scoped for now.
- [ ] Optional: WorkspaceRole alias for clarity (don’t persist alias).

Repository and persistence tweaks
- [ ] Prefer projections for read paths:
      - MembershipView (membershipId, userId, workspaceId, workspaceName, roles, status, joinedAt)
      - Repository methods returning the projection for list endpoints.
- [ ] Default filters to exclude INACTIVE memberships in common queries.
- [ ] Concurrency guard for "last membership" rule:
      - Single transaction with SELECT … FOR UPDATE around count + delete, or delete with a precondition.
- [ ] Ensure DB indexes for workspace_memberships: (user_id), (workspace_id), and composite (user_id, workspace_id).

Patch/validation rules
- [ ] Add business validation in updateMembership:
      - Prevent removing OWNER role if the user is the only owner in the workspace.
      - Validate status transitions (disallow INACTIVE when it violates global user membership rules, if applicable).
- [ ] Keep entity patching; enforce invariants in service before save.

Logging and metrics
- [ ] Demote routine info logs to debug in MembershipService and KeycloakAdminService when no changes occur.
- [ ] Add Micrometer counters/timers for membership mutations and Keycloak sync attempts/success/failure.

Migration and rollout
- [ ] Commit AttributeContributor and KeycloakUserClient; wire membership contributor.
- [ ] Switch KeycloakAdminService to aggregator + wrapper; smoke test against non-prod Keycloak.
- [ ] Introduce WorkspaceService and route default creation through it; keep old entry points temporarily if needed.
- [ ] Introduce MembershipQuery read paths; migrate controllers incrementally.

Non-goals (explicitly not in scope now)
- Event publication/consumption.
- Global roles beyond workspace scope.
- Cross-service orchestration or SSO changes.