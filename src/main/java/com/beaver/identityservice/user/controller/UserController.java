package com.beaver.identityservice.user.controller;

import com.beaver.identityservice.common.annotations.UserId;
import com.beaver.identityservice.user.dto.UserDto;
import com.beaver.identityservice.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final IUserService userService;

    @GetMapping(value = "/self", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getSelf(@UserId UUID userId) {
        return ResponseEntity.ok().body(userService.findById(userId));
    }

    @PostMapping("/bootstrap")
    public ResponseEntity<Void> bootstrap(@AuthenticationPrincipal Jwt jwt) {
        userService.bootstrap(jwt);
        return ResponseEntity.noContent().build();
    }
}
