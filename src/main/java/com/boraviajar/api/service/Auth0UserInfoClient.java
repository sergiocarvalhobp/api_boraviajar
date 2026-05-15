package com.boraviajar.api.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class Auth0UserInfoClient {

    public Auth0Profile fetchUserInfo(String domain, String accessToken) {
        String host = domain.trim().replaceFirst("^https?://", "");
        try {
            Auth0Profile body = RestClient.create()
                    .get()
                    .uri("https://{host}/userinfo", host)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(Auth0Profile.class);
            if (body == null) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Token Auth0 inválido ou expirado.");
            }
            return body;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Token Auth0 inválido ou expirado.");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Auth0Profile(String sub, String name, String nickname, String email, String picture) {}
}
