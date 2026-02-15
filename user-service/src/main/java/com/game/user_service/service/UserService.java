package com.game.user_service.service;

import com.game.user_service.auth.*;
import com.game.user_service.dto.*;
import com.game.user_service.exception.BadRequestException;
import com.game.user_service.exception.NotFoundException;
import com.game.user_service.model.UserProfile;
import com.game.user_service.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final UserMapper mapper;
    private final GoogleOAuthService googleOAuthService;
    private final JwtTokenService jwtTokenService;

    public UserService(UserProfileRepository userProfileRepository,
                       UserMapper mapper,
                       GoogleOAuthService googleOAuthService,
                       JwtTokenService jwtTokenService) {
        this.userProfileRepository = userProfileRepository;
        this.mapper = mapper;
        this.googleOAuthService = googleOAuthService;
        this.jwtTokenService = jwtTokenService;
    }

    // Kept for local/dev fallback login.
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase(Locale.ROOT).trim();
        UserProfile user = findOrCreateByEmail(email, request.getDisplayName().trim());

        LoginResponse response = new LoginResponse();
        response.setUser(mapper.toView(user));
        response.setToken("legacy-login-no-jwt");
        return response;
    }

    @Transactional
    public AuthResponse exchangeGoogleCode(GoogleExchangeRequest request) {
        GoogleUserInfo googleUser = googleOAuthService.exchangeCode(request.getCode(), request.getCodeVerifier());

        String normalizedEmail = googleUser.getEmail().toLowerCase(Locale.ROOT).trim();
        String displayName = googleUser.getName() == null || googleUser.getName().isBlank()
                ? normalizedEmail
                : googleUser.getName().trim();

        UserProfile user = upsertGoogleUser(googleUser.getSub(), normalizedEmail, displayName);

        AuthResponse response = jwtTokenService.issueTokens(user);
        response.setUser(mapper.toView(user));
        return response;
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        UUID userId = jwtTokenService.validateRefreshToken(request.getRefreshToken());
        if (userId == null) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        UserProfile user = getUserEntity(userId);
        jwtTokenService.rotateRefreshToken(request.getRefreshToken());
        AuthResponse response = jwtTokenService.issueTokens(user);
        response.setUser(mapper.toView(user));
        return response;
    }

    @Transactional(readOnly = true)
    public UserView getUser(UUID userId) {
        return mapper.toView(getUserEntity(userId));
    }

    @Transactional(readOnly = true)
    public FriendsView getFriends(UUID userId) {
        UserProfile user = getUserEntity(userId);
        FriendsView v = new FriendsView();
        v.setUserId(userId);
        v.setFriendEmails(user.getFriendEmails().stream().sorted().toList());
        return v;
    }

    @Transactional
    public FriendsView addFriend(UUID userId, FriendRequest request) {
        UserProfile user = getUserEntity(userId);
        String friendEmail = request.getFriendEmail().toLowerCase(Locale.ROOT).trim();

        if (friendEmail.equals(user.getEmail())) {
            throw new BadRequestException("Cannot add yourself as friend");
        }

        user.getFriendEmails().add(friendEmail);
        user.setUpdatedAt(Instant.now());
        userProfileRepository.save(user);
        return getFriends(userId);
    }

    @Transactional
    public FriendsView removeFriend(UUID userId, String friendEmail) {
        UserProfile user = getUserEntity(userId);
        user.getFriendEmails().remove(friendEmail.toLowerCase(Locale.ROOT).trim());
        user.setUpdatedAt(Instant.now());
        userProfileRepository.save(user);
        return getFriends(userId);
    }

    private UserProfile upsertGoogleUser(String googleSub, String email, String displayName) {
        UserProfile existingBySub = userProfileRepository.findByGoogleSub(googleSub).orElse(null);
        if (existingBySub != null) {
            existingBySub.setEmail(email);
            existingBySub.setDisplayName(displayName);
            existingBySub.setUpdatedAt(Instant.now());
            return userProfileRepository.save(existingBySub);
        }

        UserProfile existingByEmail = userProfileRepository.findByEmail(email).orElse(null);
        if (existingByEmail != null) {
            existingByEmail.setGoogleSub(googleSub);
            existingByEmail.setDisplayName(displayName);
            existingByEmail.setUpdatedAt(Instant.now());
            return userProfileRepository.save(existingByEmail);
        }

        UserProfile created = new UserProfile();
        created.setGoogleSub(googleSub);
        created.setEmail(email);
        created.setDisplayName(displayName);
        created.setCreatedAt(Instant.now());
        created.setUpdatedAt(Instant.now());
        return userProfileRepository.save(created);
    }

    private UserProfile findOrCreateByEmail(String email, String displayName) {
        UserProfile existing = userProfileRepository.findByEmail(email).orElse(null);
        if (existing != null) {
            existing.setDisplayName(displayName);
            existing.setUpdatedAt(Instant.now());
            return userProfileRepository.save(existing);
        }

        UserProfile created = new UserProfile();
        created.setEmail(email);
        created.setDisplayName(displayName);
        created.setCreatedAt(Instant.now());
        created.setUpdatedAt(Instant.now());
        return userProfileRepository.save(created);
    }

    private UserProfile getUserEntity(UUID userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }
}
