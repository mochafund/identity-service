package com.beaver.identityservice.config;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import lombok.Getter;
import lombok.Setter;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class KeycloakAdminConfig {

    @Bean
    @ConfigurationProperties(prefix = "keycloak.admin")
    public Props keycloakAdminProps() { return new Props(); }

    @Bean(destroyMethod = "close")
    public Keycloak keycloak(Props p) {
        Client jaxrs = ClientBuilder.newBuilder()
                .connectTimeout(p.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(p.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                .build();

        return KeycloakBuilder.builder()
                .serverUrl(p.getBaseUrl())
                .realm(p.getRealm())
                .clientId(p.getClientId())
                .clientSecret(p.getClientSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .resteasyClient(jaxrs)
                .build();
    }

    @Getter @Setter
    public static class Props {
        private String baseUrl, realm, clientId, clientSecret;
        private int connectTimeoutMs = 3000, readTimeoutMs = 10000;
    }
}
