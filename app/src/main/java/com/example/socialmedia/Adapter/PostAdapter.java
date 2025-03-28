package com.example.socialmedia.Adapter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmedia.Activity.CommentActivity;
import com.example.socialmedia.Activity.ReelActivity;
import com.example.socialmedia.Activity.UserProfileActivity;
import com.example.socialmedia.Model.PostModel;
import com.example.socialmedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<PostModel> postList;
    private DatabaseReference followRef, likeRef, commentRef;
    private FirebaseAuth auth;
    private MediaPlayer mediaPlayer;

    public PostAdapter(Context context, List<PostModel> postList) {
        this.context = context;
        this.postList = postList;
        this.auth = FirebaseAuth.getInstance();
        this.followRef = FirebaseDatabase.getInstance().getReference("Follow");
        this.likeRef = FirebaseDatabase.getInstance().getReference("Likes");
        this.commentRef = FirebaseDatabase.getInstance().getReference("Comments");
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostModel post = postList.get(position);
        String currentUserId = auth.getCurrentUser().getUid();
        String postUserId = post.getUserId();
        String postId = post.getPostId();

        // Open comment activity when comment button is clicked
        holder.commentBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("postId", postId);
            context.startActivity(intent);
        });

        // Fetch and update comment count
        updateCommentCount(holder.commentCount, postId);

        // Fetch and display username
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(postUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("username").getValue() != null) {
                    String fetchedUsername = snapshot.child("username").getValue(String.class);
                    holder.username.setText(fetchedUsername);
                } else {
                    holder.username.setText("Unknown User");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        holder.caption.setText(post.getCaption());

        DatabaseReference useRef = FirebaseDatabase.getInstance().getReference("Users").child(postUserId);
        useRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("profileImageUrl").getValue() != null) {
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    Glide.with(context).load(profileImageUrl).placeholder(R.drawable.avtar).into(holder.profileImage);
                } else {
                    holder.profileImage.setImageResource(R.drawable.avtar); // Default avatar if no image found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });


//        Glide.with(context).load(post.getProfileImageUrl()).placeholder(R.drawable.avtar).into(holder.profileImage);

        if ("video".equals(post.getMediaType())) {
            holder.imagePost.setVisibility(View.GONE);
            holder.relativeLayout.setVisibility(View.VISIBLE);
            holder.videoPost.setVisibility(View.VISIBLE);
            holder.muteBotton.setVisibility(View.VISIBLE);
            holder.videoPost.setVideoPath(post.getMediaUrl());
            holder.videoPost.setOnPreparedListener(mp -> {
                holder.mediaPlayer = mp;
                holder.mediaPlayer.setVolume(0f, 0f);
                mp.start();
            });
            holder.muteBotton.setImageResource(R.drawable.mute);
            holder.muteBotton.setOnClickListener(v -> toggleMute(holder));
            holder.videoPost.setOnClickListener(v -> openReelFullScreen(post));
        } else {
            holder.relativeLayout.setVisibility(View.GONE);
            holder.videoPost.setVisibility(View.GONE);
            holder.imagePost.setVisibility(View.VISIBLE);
            holder.muteBotton.setVisibility(View.GONE);
            Glide.with(context).load(post.getMediaUrl()).placeholder(R.drawable.avtar).into(holder.imagePost);
        }


        // Navigate to user profile
        View.OnClickListener profileClickListener = v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("userId", postUserId);
            context.startActivity(intent);
        };
        holder.username.setOnClickListener(profileClickListener);
        holder.profileImage.setOnClickListener(profileClickListener);

        // Follow button logic
        if (currentUserId.equals(postUserId)) {
            holder.followButton.setVisibility(View.GONE);
        } else {
            holder.followButton.setVisibility(View.VISIBLE);
            updateFollowButton(holder.followButton, currentUserId, postUserId);
            holder.followButton.setOnClickListener(v -> toggleFollow(currentUserId, postUserId));
        }

        updateLikeButton(holder.likeButton, holder.likeCount, postId, currentUserId);
        holder.likeButton.setOnClickListener(v -> toggleLike(postId, currentUserId));
        holder.shareButton.setOnClickListener(v -> sharePost(post));
    }

    private void toggleMute(PostViewHolder holder) {
        if (holder.mediaPlayer != null){
            if (holder.isMuted){
                holder.mediaPlayer.setVolume(1f,1f);
                holder.muteBotton.setImageResource(R.drawable.volume);
            }else {
                holder.mediaPlayer.setVolume(0f,0f);
                holder.muteBotton.setImageResource(R.drawable.mute);
            }
            holder.isMuted = !holder.isMuted;
        }
    }

    private void openReelFullScreen(PostModel post) {
        Intent intent = new Intent(context, ReelActivity.class);
        intent.putExtra("mediaUrl", post.getMediaUrl());
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }
    private void sharePost(PostModel post) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareText = "**" + post.getCaption() + "**\n\n" + "Check out this post on our Social Media App!\n" + (post.getMediaUrl() != null ? post.getMediaUrl() : "");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        context.startActivity(Intent.createChooser(shareIntent, "Share Post via"));
    }

    private void toggleFollow(String currentUserId, String postUserId) {
        DatabaseReference userFollowRef = followRef.child(currentUserId).child("following").child(postUserId);
        DatabaseReference otherUserFollowersRef = followRef.child(postUserId).child("followers").child(currentUserId);

        userFollowRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                userFollowRef.removeValue();
                otherUserFollowersRef.removeValue();
            } else {
                userFollowRef.setValue(true);
                otherUserFollowersRef.setValue(true);
            }
        });
    }

    private void updateFollowButton(Button followButton, String currentUserId, String postUserId) {
        followRef.child(currentUserId).child("following").child(postUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            followButton.setText("Unfollow");
                        } else {
                            followButton.setText("Follow");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void toggleLike(String postId, String currentUserId) {
        DatabaseReference userLikeRef = likeRef.child(postId).child(currentUserId);

        userLikeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                userLikeRef.removeValue();
            } else {
                userLikeRef.setValue(true);
            }
        });
    }

    private void updateLikeButton(ImageView likeButton, TextView likeCount, String postId, String currentUserId) {
        likeRef.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                likeCount.setText(count + "");

                if (snapshot.hasChild(currentUserId)) {
                    likeButton.setImageResource(R.drawable.ic_like); // Change to liked image
                    likeButton.setTag("liked"); // Set a tag to track state
                } else {
                    likeButton.setImageResource(R.drawable.ic_unlike); // Change to default image
                    likeButton.setTag("unliked"); // Set a tag to track state
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        likeButton.setOnClickListener(v -> {
            if ("liked".equals(likeButton.getTag())) {
                likeRef.child(postId).child(currentUserId).removeValue();
            } else {
                likeRef.child(postId).child(currentUserId).setValue(true);
            }
        });
    }

    // Function to fetch comment count from Firebase
    private void updateCommentCount(TextView commentCount, String postId) {
        commentRef.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                commentCount.setText(count + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView username, caption, likeCount, commentCount;
        ImageView profileImage, imagePost, likeButton, commentBtn,shareButton,muteBotton;
        VideoView videoPost;
        Button followButton;
        MediaPlayer mediaPlayer;
        boolean isMuted = true;
        RelativeLayout relativeLayout;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.usernameItem);
            caption = itemView.findViewById(R.id.caption);
            profileImage = itemView.findViewById(R.id.profileImageItem);
            imagePost = itemView.findViewById(R.id.imagePost);
            videoPost = itemView.findViewById(R.id.videoPost);
            followButton = itemView.findViewById(R.id.followButtonIP);
            likeButton = itemView.findViewById(R.id.likeButton);
            commentCount = itemView.findViewById(R.id.commentCount);
            commentBtn = itemView.findViewById(R.id.commentButton);
            likeCount = itemView.findViewById(R.id.likeCount);
            shareButton = itemView.findViewById(R.id.shareButton);
            muteBotton = itemView.findViewById(R.id.muteButtonP);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);
        }
    }
}
