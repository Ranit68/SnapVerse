package com.example.socialmedia.Model;

public class PostModel {
    private String userId;
    private String username;
    private String caption;
    private String description;
    private String mediaUrl;
    private String postId;
    private String mediaType; // "image" or "video"
    private String profileImageUrl;
    private long timestamp;
    public PostModel() {
        // Default constructor required for Firebase
    }
    public PostModel(String userId, String username, String caption, String description, String mediaUrl, String mediaType, String profileImageUrl, long timestamp,String postId) {
        this.userId = userId;
        this.username = username;
        this.caption = caption;
        this.description = description;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.profileImageUrl = profileImageUrl;
        this.postId = postId;
        this.timestamp = timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getCaption() {
        return caption;
    }

    public String getDescription() {
        return description;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
