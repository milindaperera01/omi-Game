package com.game.user_service.auth;

import com.game.user_service.config.AppProperties;
import com.game.user_service.exception.BadRequestException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class GoogleOAuthService {

    private static final List<String> VALID_ISSUERS = List.of("accounts.google.com", "https://accounts.google.com");

    private final AppProperties properties;
    private final RestClient restClient;

    public GoogleOAuthService(AppProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(8));
        requestFactory.setReadTimeout(Duration.ofSeconds(12));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    public GoogleUserInfo exchangeCode(String code, String codeVerifier) {
        validateConfig();

        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("code", code);
        payload.add("client_id", properties.getGoogle().getClientId());
        payload.add("client_secret", properties.getGoogle().getClientSecret());
        payload.add("redirect_uri", properties.getGoogle().getRedirectUri());
        payload.add("grant_type", "authorization_code");
        if (codeVerifier != null && !codeVerifier.isBlank()) {
            payload.add("code_verifier", codeVerifier);
        }

        GoogleTokenResponse tokenResponse;
        try {
            tokenResponse = restClient.post()
                    .uri(properties.getGoogle().getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(payload)
                    .retrieve()
                    .body(GoogleTokenResponse.class);
        } catch (RestClientException ex) {
            throw new BadRequestException("Google token exchange failed: " + ex.getMessage());
        }

        if (tokenResponse == null || tokenResponse.getIdToken() == null || tokenResponse.getIdToken().isBlank()) {
            throw new BadRequestException("Google token response did not include id_token");
        }

        return verifyIdToken(tokenResponse.getIdToken());
    }

    private GoogleUserInfo verifyIdToken(String idToken) {
        try {
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(properties.getGoogle().getJwksUri()));
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);
            jwtProcessor.setJWSKeySelector(keySelector);

            JWTClaimsSet claims = jwtProcessor.process(idToken, null);

            if (!VALID_ISSUERS.contains(claims.getIssuer())) {
                throw new BadRequestException("Invalid Google token issuer");
            }
            if (claims.getAudience() == null || !claims.getAudience().contains(properties.getGoogle().getClientId())) {
                throw new BadRequestException("Google token audience mismatch");
            }
            if (claims.getExpirationTime() == null || claims.getExpirationTime().toInstant().isBefore(Instant.now())) {
                throw new BadRequestException("Google token expired");
            }

            Object emailVerified = claims.getClaim("email_verified");
            if (!(emailVerified instanceof Boolean verified) || !verified) {
                throw new BadRequestException("Google account email is not verified");
            }

            GoogleUserInfo info = new GoogleUserInfo();
            info.setSub(claims.getSubject());
            info.setEmail(claims.getStringClaim("email"));
            info.setName(claims.getStringClaim("name"));
            return info;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Failed to verify Google token");
        }
    }

    private void validateConfig() {
        if (isBlank(properties.getGoogle().getClientId())
                || isBlank(properties.getGoogle().getClientSecret())
                || isBlank(properties.getGoogle().getRedirectUri())) {
            throw new BadRequestException("Google OAuth is not configured");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static class GoogleTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("id_token")
        private String idToken;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getIdToken() {
            return idToken;
        }

        public void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
}
