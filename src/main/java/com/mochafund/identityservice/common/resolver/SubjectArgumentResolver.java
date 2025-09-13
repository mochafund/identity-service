package com.mochafund.identityservice.common.resolver;

import com.mochafund.identityservice.common.annotations.Subject;
import com.mochafund.identityservice.common.exception.BadRequestException;
import com.mochafund.identityservice.common.exception.UnauthorizedException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public class SubjectArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Subject.class) &&
                parameter.getParameterType().equals(UUID.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new UnauthorizedException("No JWT token found");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new BadRequestException("JWT missing subject claim");
        }

        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid subject format in JWT");
        }
    }
}
