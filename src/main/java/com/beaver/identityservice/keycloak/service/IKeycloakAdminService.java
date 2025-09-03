package com.beaver.identityservice.keycloak.service;

import com.beaver.identityservice.user.entity.User;

public interface IKeycloakAdminService {
    void syncAttributes(String sub, User user);
    void logoutAllSessions(String sub);
    void deleteUser(String sub);
}
