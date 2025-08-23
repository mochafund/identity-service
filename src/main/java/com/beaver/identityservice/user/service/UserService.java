package com.beaver.identityservice.user.service;

import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.user.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements IUserService {

    private final IUserRepository userRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            log.debug("Found user with id: {}", user.get().getId());
        } else {
            log.debug("No user found with email: {}", email);
        }
        return user;
    }

    @Override
    public User save(User user) {
        log.debug("Saving user: {}", user.getEmail());
        User savedUser = userRepository.save(user);
        log.debug("Saved user with id: {}", savedUser.getId());
        return savedUser;
    }
}
