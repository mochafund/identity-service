package com.beaver.identityservice.user.controller;

import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final IUserService userService;

    @GetMapping
    public Mono<User> getUserByEmail(@RequestParam String email) {
        log.debug("Getting user by email: {}", email);
        return userService.findByEmail(email);
    }
}
