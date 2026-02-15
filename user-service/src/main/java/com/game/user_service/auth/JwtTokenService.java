package com.game.user_service.auth;

import com.game.user_service.config.AppProperties;
import com.game.user_service.model.RefreshToken;
import com.game.user_service.model.UserProfile;
import com.game.user_service.repository.RefreshTokenRepository;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final AppProperties properties;
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtTokenService(AppProperties properties,
                           JwtEncoder jwtEncoder,
                           RefreshTokenRepository refreshTokenRepository) {
        this.properties = properties;
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public AuthResponse issueTokens(UserProfile user) {
        Instant now = Instant.now();
        Instant accessExp = now.plusSeconds(properties.getJwt().getAccessTtlSeconds());
        Instant refreshExp = now.plusSeconds(properties.getJwt().getRefreshTtlSeconds());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getJwt().getIssuer())
                .issuedAt(now)
                .expiresAt(accessExp)
                .subject(user.getUserId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getDisplayName())
                .build();

        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(refreshExp);
        refreshTokenRepository.save(refreshToken);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshTokenValue);
        response.setAccessTokenExpiresAt(accessExp);
        response.setRefreshTokenExpiresAt(refreshExp);
        return response;
    }

    @Transactional
    public UUID validateRefreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findById(refreshToken).orElse(null);
        if (token == null) {
            return null;
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.deleteById(refreshToken);
            return null;
        }
        return token.getUser().getUserId();
    }

    @Transactional
    public void rotateRefreshToken(String oldToken) {
        refreshTokenRepository.deleteById(oldToken);
    }
}
