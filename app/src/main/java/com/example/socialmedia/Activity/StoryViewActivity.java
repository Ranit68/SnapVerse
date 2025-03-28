package com.example.socialmedia.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.socialmedia.R;
import com.example.socialmedia.Model.StoryModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StoryViewActivity extends AppCompatActivity {
    private ImageView storyImage, reactButton, sendReply;
    private VideoView storyVideo;
    private EditText replyInput;

    private List<StoryModel> storyList = new ArrayList<>();
    private int currentStoryIndex = 0;
    private Handler storyHandler = new Handler();

    private String userId;

    private static final int STORY_DURATION = 5000; // 5 seconds per story

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_view);

        storyImage = findViewById(R.id.storyImageView);
        storyVideo = findViewById(R.id.storyVideoView);
        replyInput = findViewById(R.id.replyInput);
        sendReply = findViewById(R.id.sendReply);
        reactButton = findViewById(R.id.reactButton);

        userId = getIntent().getStringExtra("userId");

        Log.d("StoryViewActivity", "Received userId: " + userId);

        if (userId == null) {
            Toast.makeText(this, "Failed to load story. Try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserStories(userId);

        sendReply.setOnClickListener(v -> sendReplyMessage());
        reactButton.setOnClickListener(v -> reactToStory());

        // Handle tap gestures for next/previous story
        View storyViewContainer = findViewById(R.id.storyViewContainer);
        storyViewContainer.setOnTouchListener((v, event) -> handleStoryNavigation(event));
    }

    private void loadUserStories(String userId) {
        DatabaseReference storyRef = FirebaseDatabase.getInstance().getReference("Stories").child(userId);

        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    StoryModel story = dataSnapshot.getValue(StoryModel.class);
                    if (story != null) {
                        storyList.add(story);
                    }
                }

                if (!storyList.isEmpty()) {
                    displayStory(currentStoryIndex);
                    autoPlayNextStory();
                } else {
                    Log.e("StoryView", "No stories found for user: " + userId);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StoryView", "Error loading stories: " + error.getMessage());
            }
        });
    }

    private void displayStory(int index) {
        if (index < 0 || index >= storyList.size()) {
            finish(); // Close activity when stories end
            return;
        }

        StoryModel currentStory = storyList.get(index);
        String mediaUrl = currentStory.getMediaUrl();
        String storyType = currentStory.getStoryType();

        if ("video".equals(storyType)) {
            storyVideo.setVisibility(View.VISIBLE);
            storyImage.setVisibility(View.GONE);
            storyVideo.setVideoPath(mediaUrl);
            storyVideo.start();
        } else {
            storyImage.setVisibility(View.VISIBLE);
            storyVideo.setVisibility(View.GONE);
            Glide.with(this).load(mediaUrl).into(storyImage);
        }
    }

    private void autoPlayNextStory() {
        storyHandler.postDelayed(() -> {
            if (currentStoryIndex < storyList.size() - 1) {
                currentStoryIndex++;
                displayStory(currentStoryIndex);
                autoPlayNextStory();
            } else {
                finish(); // Close activity when all stories are viewed
            }
        }, STORY_DURATION);
    }

    private boolean handleStoryNavigation(MotionEvent event) {
        float x = event.getX();
        float screenWidth = findViewById(R.id.storyViewContainer).getWidth();

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (x > screenWidth * 0.7) {
                nextStory();
            } else if (x < screenWidth * 0.3) {
                previousStory();
            }
        }
        return true;
    }

    private void nextStory() {
        if (currentStoryIndex < storyList.size() - 1) {
            currentStoryIndex++;
            displayStory(currentStoryIndex);
            autoPlayNextStory();
        } else {
            finish();
        }
    }

    private void previousStory() {
        if (currentStoryIndex > 0) {
            currentStoryIndex--;
            displayStory(currentStoryIndex);
        }
    }

    private void sendReplyMessage() {
        String replyText = replyInput.getText().toString().trim();
        if (!replyText.isEmpty()) {
            DatabaseReference replyRef = FirebaseDatabase.getInstance().getReference("StoryReplies")
                    .child(userId)
                    .child(storyList.get(currentStoryIndex).getStoryId());

            String replyId = replyRef.push().getKey();
            replyRef.child(replyId).setValue(replyText);
            replyInput.setText("");

            Toast.makeText(this, "Reply sent!", Toast.LENGTH_SHORT).show();
        }
    }

    private void reactToStory() {
        DatabaseReference reactRef = FirebaseDatabase.getInstance().getReference("StoryReacts")
                .child(userId)
                .child(storyList.get(currentStoryIndex).getStoryId());

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        reactRef.child(currentUserId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()){
                reactRef.child(currentUserId).removeValue().addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Reaction removed", Toast.LENGTH_SHORT).show();
                    reactButton.setImageResource(R.drawable.ic_unlike);
                });
            }else {
                reactRef.child(currentUserId).setValue("❤️").addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Reacted ❤️", Toast.LENGTH_SHORT).show();
                    reactButton.setImageResource(R.drawable.ic_like);
                });
            }
        });
//        reactRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue("❤️");
//        Toast.makeText(this, "Reacted ❤️", Toast.LENGTH_SHORT).show();
    }
}
