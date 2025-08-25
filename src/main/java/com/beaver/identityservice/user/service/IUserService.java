package com.beaver.identityservice.user.service;

import com.beaver.identityservice.user.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface IUserService {
    Optional<User> findByEmail(String email);
    User save(User user);
    User bootstrap(Jwt jwt);
}
