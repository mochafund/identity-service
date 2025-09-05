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
}
