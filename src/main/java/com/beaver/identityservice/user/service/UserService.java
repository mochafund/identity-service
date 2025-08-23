package com.beaver.identityservice.user.service;

import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.user.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final IUserRepository userRepository;

    @Override
    public Mono<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .doOnNext(user -> log.debug("Found user with id: {}", user.getId()))
                .doOnSuccess(user -> {
                    if (user == null) log.debug("No user found with email: {}", email);
                });
    }
}
