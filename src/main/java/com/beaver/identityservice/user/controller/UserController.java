package com.beaver.identityservice.user.controller;

import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    public ResponseEntity<Map<String, UUID>> bootstrap(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        log.debug("Bootstrap user for email: {}", email);

        Optional<User> existing = userService.findByEmail(email);
        if (existing.isPresent()) {
            UUID id = existing.get().getId();
            return ResponseEntity.ok(Map.of("userId", id));
        }

        String name = Optional.ofNullable(jwt.getClaimAsString("name")).orElse(email);
        User created = userService.save(User.builder()
                .email(email)
                .name(name)
                .isActive(true)
                .build());
        return ResponseEntity.status(201).body(Map.of("userId", created.getId()));
    }
}
