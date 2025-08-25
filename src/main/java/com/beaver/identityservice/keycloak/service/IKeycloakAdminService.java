package com.beaver.identityservice.keycloak.service;

public interface IKeycloakAdminService {
    void upsertUserAttribute(String sub, String key, String value);
    void logoutAllSessions(String sub);
    void deleteUser(String sub);
}
