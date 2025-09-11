package com.mochafund.identityservice.user.controller;

import com.mochafund.identityservice.common.annotations.UserId;
import com.mochafund.identityservice.user.dto.UpdateUserDto;
import com.mochafund.identityservice.user.dto.UserDto;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.user.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final IUserService userService;

    @PostMapping(value = "/bootstrap")
    public ResponseEntity<Void> bootstrap(@AuthenticationPrincipal Jwt jwt) {
        userService.bootstrap(jwt);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/self", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getSelf(@UserId UUID userId) {
        User user = userService.getById(userId);
        return ResponseEntity.ok().body(UserDto.fromEntity(user));
    }

    @PatchMapping(value = "/self", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> updateSelf(
            @UserId UUID userId, @Valid @RequestBody UpdateUserDto userDto) {
        User updatedUser = userService.updateById(userId, userDto);
        return ResponseEntity.ok().body(UserDto.fromEntity(updatedUser));
    }

    @DeleteMapping(value = "/self")
    public ResponseEntity<Void> deleteSelf(@UserId UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
