package com.mochafund.identityservice.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WhoAmIController {
    @GetMapping("/whoami")
    public Map<String, Object> claims(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaims();
    }
}