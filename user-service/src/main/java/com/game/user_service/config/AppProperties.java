package com.game.user_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Google google = new Google();
    private final Jwt jwt = new Jwt();

    public Google getGoogle() {
        return google;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public static class Google {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String tokenUri;
        private String jwksUri;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }

        public String getTokenUri() {
            return tokenUri;
        }

        public void setTokenUri(String tokenUri) {
            this.tokenUri = tokenUri;
        }

        public String getJwksUri() {
            return jwksUri;
        }

        public void setJwksUri(String jwksUri) {
            this.jwksUri = jwksUri;
        }
    }

    public static class Jwt {
        private String issuer;
        private long accessTtlSeconds;
        private long refreshTtlSeconds;
        private String privateKeyBase64Pkcs8;
        private String publicKeyBase64X509;

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public long getAccessTtlSeconds() {
            return accessTtlSeconds;
        }

        public void setAccessTtlSeconds(long accessTtlSeconds) {
            this.accessTtlSeconds = accessTtlSeconds;
        }

        public long getRefreshTtlSeconds() {
            return refreshTtlSeconds;
        }

        public void setRefreshTtlSeconds(long refreshTtlSeconds) {
            this.refreshTtlSeconds = refreshTtlSeconds;
        }

        public String getPrivateKeyBase64Pkcs8() {
            return privateKeyBase64Pkcs8;
        }

        public void setPrivateKeyBase64Pkcs8(String privateKeyBase64Pkcs8) {
            this.privateKeyBase64Pkcs8 = privateKeyBase64Pkcs8;
        }

        public String getPublicKeyBase64X509() {
            return publicKeyBase64X509;
        }

        public void setPublicKeyBase64X509(String publicKeyBase64X509) {
            this.publicKeyBase64X509 = publicKeyBase64X509;
        }
    }
}
