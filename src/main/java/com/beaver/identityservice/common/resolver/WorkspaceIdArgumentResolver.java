package com.beaver.identityservice.common.resolver;

import com.beaver.identityservice.common.annotations.WorkspaceId;
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

public class WorkspaceIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(WorkspaceId.class) &&
               parameter.getParameterType().equals(UUID.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No JWT token found");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String workspaceId = jwt.getClaimAsString("workspace_id");

        if (workspaceId == null || workspaceId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JWT missing workspace_id claim");
        }

        try {
            return UUID.fromString(workspaceId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid workspace_id format in JWT");
        }
    }
}
