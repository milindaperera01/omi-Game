package com.game.user_service.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

@Configuration
public class JwtConfig {

    @Bean
    public RSAKey rsaKey(AppProperties properties) {
        try {
            String privateKeyBase64 = properties.getJwt().getPrivateKeyBase64Pkcs8();
            String publicKeyBase64 = properties.getJwt().getPublicKeyBase64X509();

            if (privateKeyBase64 != null && !privateKeyBase64.isBlank()
                    && publicKeyBase64 != null && !publicKeyBase64.isBlank()) {
                byte[] privateBytes = Base64.getDecoder().decode(privateKeyBase64.getBytes(StandardCharsets.UTF_8));
                byte[] publicBytes = Base64.getDecoder().decode(publicKeyBase64.getBytes(StandardCharsets.UTF_8));

                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
                RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));

                return new RSAKey.Builder(publicKey)
                        .privateKey(privateKey)
                        .keyID("omi-key-1")
                        .build();
            }

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID("dev-" + UUID.randomUUID())
                    .build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to initialize RSA key", ex);
        }
    }

    @Bean
    public JWKSet jwkSet(RSAKey rsaKey) {
        return new JWKSet(rsaKey.toPublicJWK());
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey) {
        return new NimbusJwtEncoder((jwkSelector, context) -> jwkSelector.select(new JWKSet(rsaKey)));
    }
}
