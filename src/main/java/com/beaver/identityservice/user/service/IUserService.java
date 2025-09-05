package com.beaver.identityservice.user.service;

import com.beaver.identityservice.user.dto.UserDto;
import com.beaver.identityservice.user.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

public interface IUserService {
    UserDto findById(UUID id);
    Optional<User> findByEmail(String email);
    User save(User user);
    void bootstrap(Jwt jwt);
}
