package com.example.socialmedia.Model;

public class UserModel {
    private String userId;
    private String username;
    private String email;
    private String profileImageUrl;
    private int followers;
    private int following;

    public UserModel() {
        // Empty constructor required for Firebase
    }

    public UserModel(String userId, String username, String email, String profileImageUrl, int followers, int following) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.followers = followers;
        this.following = following;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public int getFollowers() {
        return followers;
    }

    public int getFollowing() {
        return following;
    }
}
