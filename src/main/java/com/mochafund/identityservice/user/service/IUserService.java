package com.mochafund.identityservice.user.service;

import com.mochafund.identityservice.user.dto.UpdateUserDto;
import com.mochafund.identityservice.user.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public interface IUserService {
    void bootstrap(Jwt jwt);
    User updateUser(UUID userId, UpdateUserDto userDto);
    User getUser(UUID userId);
    User save(User user);
    void deleteUser(UUID userId);
}
