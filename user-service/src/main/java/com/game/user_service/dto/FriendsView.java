package com.game.user_service.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendsView {
    private UUID userId;
    private List<String> friendEmails = new ArrayList<>();

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<String> getFriendEmails() {
        return friendEmails;
    }

    public void setFriendEmails(List<String> friendEmails) {
        this.friendEmails = friendEmails;
    }
}
