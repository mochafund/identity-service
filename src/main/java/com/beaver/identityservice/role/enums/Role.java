package com.beaver.identityservice.role.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {
    READ(0),
    WRITE(1),
    ADMIN(2),
    OWNER(3);

    private final int level;

    /**
     * Check if this role has access to perform operations requiring the specified role
     * @param requiredRole The minimum role level required
     * @return true if this role's level is >= required role's level
     */
    public boolean hasAccess(Role requiredRole) {
        return this.level >= requiredRole.level;
    }

    /**
     * Check if this role can manage (assign/remove) the target role
     * Only higher roles can manage lower roles, and owners can manage all roles
     * @param targetRole The role to be managed
     * @return true if this role can manage the target role
     */
    public boolean canManageRole(Role targetRole) {
        // Owners can manage all roles, others can only manage roles below them
        return this == OWNER || this.level > targetRole.level;
    }

    @Override
    public String toString() {
        return name();
    }
}
