package com.example.socialmedia.Model;

public class StoryModel {
    private String storyId, userId, mediaUrl, storyType;
    private long timestamp;

    public StoryModel() {}

    public StoryModel(String storyId, String userId, String mediaUrl, String storyType, long timestamp) {
        this.storyId = storyId;
        this.userId = userId;
        this.mediaUrl = mediaUrl;
        this.storyType = storyType;
        this.timestamp = timestamp;
    }

    public String getStoryId() { return storyId; }
    public String getUserId() { return userId; }
    public String getMediaUrl() { return mediaUrl; }
    public String getStoryType() { return storyType; }
    public long getTimestamp() { return timestamp; }
}
