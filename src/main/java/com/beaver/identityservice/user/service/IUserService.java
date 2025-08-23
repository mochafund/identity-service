package com.beaver.identityservice.user.service;

import com.beaver.identityservice.user.entity.User;
import reactor.core.publisher.Mono;

public interface IUserService {
    Mono<User> findByEmail(String email);
}
