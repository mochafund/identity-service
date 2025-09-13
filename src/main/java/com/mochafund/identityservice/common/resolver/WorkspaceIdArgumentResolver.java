package com.mochafund.identityservice.common.resolver;

import com.mochafund.identityservice.common.annotations.WorkspaceId;
import com.mochafund.identityservice.common.exception.BadRequestException;
import com.mochafund.identityservice.common.exception.UnauthorizedException;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

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
            throw new UnauthorizedException("No JWT token found");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String workspaceId = jwt.getClaimAsString("workspace_id");

        if (workspaceId == null || workspaceId.isBlank()) {
            throw new BadRequestException("JWT missing workspace_id claim");
        }

        try {
            return UUID.fromString(workspaceId);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid workspace_id format in JWT");
        }
    }
}
