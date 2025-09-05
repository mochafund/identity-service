package com.beaver.identityservice.user.controller;

import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final IUserService userService;

    @GetMapping(value = "/self",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getSelf(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("user_id");
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JWT missing User ID (userId) claim");
        }

        UUID userUuid;
        try {
            userUuid = UUID.fromString(userId);
            log.debug("USER-UUID={} in JWT", userUuid);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID format in JWT");
        }

        return ResponseEntity.ok(userService.findById(userUuid).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
    }

    @PostMapping("/bootstrap")
    public ResponseEntity<Void> bootstrap(@AuthenticationPrincipal Jwt jwt) {
        userService.bootstrap(jwt);
        return ResponseEntity.noContent().build();
    }
}
