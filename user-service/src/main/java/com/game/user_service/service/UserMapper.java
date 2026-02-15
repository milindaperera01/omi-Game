package com.game.user_service.service;

import com.game.user_service.dto.UserView;
import com.game.user_service.model.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserView toView(UserProfile user) {
        UserView v = new UserView();
        v.setUserId(user.getUserId());
        v.setEmail(user.getEmail());
        v.setDisplayName(user.getDisplayName());
        v.setCreatedAt(user.getCreatedAt());
        v.setUpdatedAt(user.getUpdatedAt());
        return v;
    }
}
