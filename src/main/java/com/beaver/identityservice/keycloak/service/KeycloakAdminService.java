package com.beaver.identityservice.keycloak.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAdminService implements IKeycloakAdminService {

    private final RestTemplate restTemplate;
    private final OAuth2AuthorizedClientManager clientManager;

    @Value("${keycloak.admin.base-url}")
    private String baseUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    /** Must match spring.security.oauth2.client.registration.<id> in application.yml */
    private static final String REGISTRATION_ID = "identity-service";

    @Override
    public void upsertUserAttribute(String sub, String key, String value) {
        final String userUrl = baseUrl + "/admin/realms/{realm}/users/{id}";
        final String token = adminAccessToken();

        HttpHeaders headers = bearer(token);

        // GET full user as Map (preserves all fields for safe PUT)
        Map<String, Object> user = getUserAsMap(headers, userUrl, sub);

        @SuppressWarnings("unchecked")
        Map<String, Object> attrs = (Map<String, Object>) user.get("attributes");
        if (attrs == null) {
            attrs = new HashMap<>();
            user.put("attributes", attrs);
        }

        List<String> current = toStringList(attrs.get(key));
        if (current != null && current.size() == 1 && Objects.equals(current.getFirst(), value)) {
            log.debug("Keycloak attribute '{}' already set for sub={}, skipping PUT", key, sub);
            return; // idempotent
        }

        attrs.put(key, List.of(value));

        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            restTemplate.exchange(userUrl, HttpMethod.PUT, new HttpEntity<>(user, headers), Void.class, realm, sub);
            log.debug("Keycloak attribute '{}' upserted for sub={} → {}", key, sub, value);
        } catch (HttpClientErrorException.NotFound e) {
            throw notFound(sub, e);
        } catch (HttpClientErrorException.Forbidden e) {
            throw forbidden(e);
        } catch (RestClientResponseException e) {
            throw badGateway("Failed to update Keycloak user attribute", e);
        }
    }

    @Override
    public void logoutAllSessions(String sub) {
        final String uri = baseUrl + "/admin/realms/{realm}/users/{id}/logout";
        HttpHeaders headers = bearer(adminAccessToken());
        try {
            restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), Void.class, realm, sub);
            log.debug("Keycloak back-channel logout done for sub={}", sub);
        } catch (HttpClientErrorException.NotFound e) {
            // Treat as success: user gone already
            log.debug("Keycloak user not found during logout (sub={}), treating as success", sub);
        } catch (HttpClientErrorException.Forbidden e) {
            throw forbidden(e);
        } catch (RestClientResponseException e) {
            throw badGateway("Failed to back-channel logout Keycloak user", e);
        }
    }

    @Override
    public void deleteUser(String sub) {
        final String uri = baseUrl + "/admin/realms/{realm}/users/{id}";
        HttpHeaders headers = bearer(adminAccessToken());
        try {
            restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class, realm, sub);
            log.debug("Keycloak user deleted sub={}", sub);
        } catch (HttpClientErrorException.NotFound e) {
            // Idempotent delete
            log.debug("Keycloak user already deleted sub={}, treating as success", sub);
        } catch (HttpClientErrorException.Forbidden e) {
            throw forbidden(e);
        } catch (RestClientResponseException e) {
            throw badGateway("Failed to delete Keycloak user", e);
        }
    }

    // ---- helpers ----

    private Map<String, Object> getUserAsMap(HttpHeaders headers, String userUrl, String sub) {
        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    userUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class, realm, sub);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) resp.getBody();
            if (body == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty Keycloak user representation");
            }
            return body;
        } catch (HttpClientErrorException.NotFound e) {
            throw notFound(sub, e);
        } catch (HttpClientErrorException.Forbidden e) {
            throw forbidden(e);
        } catch (RestClientResponseException e) {
            throw badGateway("Failed to fetch Keycloak user", e);
        }
    }

    private String adminAccessToken() {
        var principal = new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        OAuth2AuthorizeRequest req = OAuth2AuthorizeRequest
                .withClientRegistrationId(REGISTRATION_ID)
                .principal(principal)
                .build();

        OAuth2AuthorizedClient client = clientManager.authorize(req);
        if (client == null || client.getAccessToken() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to obtain Keycloak admin access token");
        }
        return client.getAccessToken().getTokenValue();
    }

    private static HttpHeaders bearer(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return h;
    }

    @SuppressWarnings("unchecked")
    private static List<String> toStringList(Object o) {
        if (o == null) return null;
        if (o instanceof List<?> l) return (List<String>) l;
        if (o instanceof String s) return List.of(s); // very old KC versions
        return null;
    }

    private static ResponseStatusException notFound(String sub, Exception e) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Keycloak user not found: " + sub, e);
    }

    private static ResponseStatusException forbidden(HttpClientErrorException.Forbidden e) {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Keycloak admin lacks permission (need realm-management: manage-users)",
                e);
    }

    private static ResponseStatusException badGateway(String msg, RestClientResponseException e) {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                msg + " (status " + e.getRawStatusCode() + ")",
                e);
    }
}
