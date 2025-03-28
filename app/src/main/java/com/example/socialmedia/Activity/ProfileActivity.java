package com.example.socialmedia.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmedia.Adapter.ProfilePostAdapter;
import com.example.socialmedia.Model.PostModel;
import com.example.socialmedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView username, followersCount, followingCount;
    private Button followButton;
    private RecyclerView postsRecyclerView;
    private List<PostModel> postList;
    private ProfilePostAdapter profilePostAdapter;
    private DatabaseReference userRef, followRef, postsRef;
    private String userId, currentUserId;
    private FirebaseAuth auth;
    private boolean isCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profileImage);
        username = findViewById(R.id.username);
        followersCount = findViewById(R.id.followersCount);
        followingCount = findViewById(R.id.followingCount);
        followButton = findViewById(R.id.followButton);
        postsRecyclerView = findViewById(R.id.postsRecyclerView);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        userId = getIntent().getStringExtra("userId");

        isCurrentUser = userId.equals(currentUserId);
        if (isCurrentUser) {
            followButton.setVisibility(View.GONE);
        }

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        followRef = FirebaseDatabase.getInstance().getReference("Follow");
        postsRef = FirebaseDatabase.getInstance().getReference("Posts");

        postList = new ArrayList<>();
        profilePostAdapter = new ProfilePostAdapter(this, postList);
        postsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        postsRecyclerView.setAdapter(profilePostAdapter);

        loadUserData();
        loadUserPosts();
        updateFollowButton();

        followButton.setOnClickListener(v -> toggleFollow());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userProfile = snapshot.child("profileImageUrl").getValue(String.class);
                    String name = snapshot.child("username").getValue(String.class);

                    Glide.with(ProfileActivity.this).load(userProfile).placeholder(R.drawable.avtar).into(profileImage);
                    username.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        followRef.child(userId).child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followersCount.setText(snapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        followRef.child(userId).child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingCount.setText(snapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadUserPosts() {
        postsRef.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PostModel post = dataSnapshot.getValue(PostModel.class);
                    postList.add(post);
                }
                profilePostAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void toggleFollow() {
        DatabaseReference userFollowRef = followRef.child(currentUserId).child("following").child(userId);
        DatabaseReference otherUserFollowersRef = followRef.child(userId).child("followers").child(currentUserId);

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

    private void updateFollowButton() {
        followRef.child(currentUserId).child("following").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        followButton.setText(snapshot.exists() ? "Unfollow" : "Follow");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }
}
