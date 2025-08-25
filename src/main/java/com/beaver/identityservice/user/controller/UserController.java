package com.beaver.identityservice.user.controller;

import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final IUserService userService;

    @GetMapping
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        log.debug("Getting user by email: {}", email);
        Optional<User> user = userService.findByEmail(email);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/bootstrap")
    public ResponseEntity<Void> bootstrap(@AuthenticationPrincipal Jwt jwt) {
        userService.bootstrap(jwt);
        return ResponseEntity.noContent().build();
    }
}
