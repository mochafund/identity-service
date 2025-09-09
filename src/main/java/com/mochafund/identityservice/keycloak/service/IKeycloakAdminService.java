package com.mochafund.identityservice.keycloak.service;

import com.mochafund.identityservice.user.entity.User;

import java.util.UUID;

public interface IKeycloakAdminService {
    void syncAttributes(String subject, User user);
    void logoutAllSessions(UUID subject);
    void deleteUser(UUID subject);
}
