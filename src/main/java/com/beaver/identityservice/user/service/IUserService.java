package com.beaver.identityservice.user.service;

import com.beaver.identityservice.user.entity.User;

import java.util.Optional;

public interface IUserService {
    Optional<User> findByEmail(String email);
    User save(User user);
}
