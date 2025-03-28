package com.example.socialmedia.Model;

public class CommentModel {
    private String commentId;
    private String postId;
    private String userId;
    private String username;
    private String profileImageUrl;
    private String commentText;
    private long timestamp;

    public CommentModel() {
    }

    public CommentModel(String commentId, String postId, String userId, String username, String profileImageUrl, String commentText, long timestamp) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    public String getCommentId() { return commentId; }
    public String getPostId() { return postId; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getCommentText() { return commentText; }
    public long getTimestamp() { return timestamp; }
}
