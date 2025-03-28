package com.example.socialmedia.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmedia.Activity.StoryViewActivity;
import com.example.socialmedia.R;
import com.example.socialmedia.Model.StoryModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {
    private final Context context;
    private final List<StoryModel> storyList;
    private final DatabaseReference storySeenRef;
    private final String currentUserId;

    public StoryAdapter(Context context, List<StoryModel> storyList) {
        this.context = context;
        this.storyList = storyList;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        this.currentUserId = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;
        this.storySeenRef = FirebaseDatabase.getInstance().getReference("StorySeen");
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        if (position >= storyList.size()) {
            Log.e("StoryAdapter", "Invalid position: " + position);
            return;
        }

        StoryModel story = storyList.get(position);

        if (story == null || story.getUserId() == null || story.getMediaUrl() == null) {
            Log.e("StoryAdapter", "Skipping story at position " + position + " due to missing data");
            return;
        }

        String storyUserId = story.getUserId();

        // Load story image with Glide
        Glide.with(context)
                .load(story.getMediaUrl())
                .placeholder(R.drawable.avtar)
                .error(R.drawable.avtar)
                .into(holder.storyImage);

        // Check if the story is seen and update UI
        if (currentUserId != null) {
            storySeenRef.child(currentUserId).child(storyUserId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                holder.storyImage.setBackgroundResource(R.drawable.story_seen_border);
                            } else {
                                holder.storyImage.setBackgroundResource(R.drawable.story_unseen_border);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("StoryAdapter", "Failed to load story status: " + error.getMessage());
                        }
                    });
        }

        // Click to open story
        holder.itemView.setOnClickListener(v -> {
            Log.d("StoryAdapter", "Story clicked: " + story.getUserId() + ", storyId: " + story.getStoryId());
            Intent intent = new Intent(context, StoryViewActivity.class);
            intent.putExtra("userId", story.getUserId());
            intent.putExtra("storyId", story.getStoryId());
            context.startActivity(intent);

            // Mark the story as seen
            if (currentUserId != null) {
                storySeenRef.child(currentUserId).child(storyUserId).setValue(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (storyList != null) ? storyList.size() : 0;
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {
        ImageView storyImage;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            storyImage = itemView.findViewById(R.id.storyImage);
        }
    }
}
