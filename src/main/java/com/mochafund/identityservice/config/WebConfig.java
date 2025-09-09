package com.mochafund.identityservice.config;

import com.mochafund.identityservice.common.resolver.SubjectArgumentResolver;
import com.mochafund.identityservice.common.resolver.UserIdArgumentResolver;
import com.mochafund.identityservice.common.resolver.WorkspaceIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserIdArgumentResolver());
        resolvers.add(new WorkspaceIdArgumentResolver());
        resolvers.add(new SubjectArgumentResolver());
    }
}
