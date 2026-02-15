package com.game.user_service.repository;

import com.game.user_service.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByEmail(String email);

    Optional<UserProfile> findByGoogleSub(String googleSub);
}
