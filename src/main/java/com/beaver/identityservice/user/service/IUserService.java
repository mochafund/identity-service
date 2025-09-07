package com.beaver.identityservice.user.service;

import com.beaver.identityservice.user.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public interface IUserService {
    User getById(UUID userId);
    User save(User user);
    void bootstrap(Jwt jwt);
    void deleteUser(UUID userId, UUID subject);
}
