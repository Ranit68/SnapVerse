package com.example.socialmedia.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.socialmedia.Activity.EditProfileActivity;
import com.example.socialmedia.Adapter.ProfilePostAdapter;
import com.example.socialmedia.Model.PostModel;
import com.example.socialmedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private ImageView profileImage;
    private TextView username, bio, followersCount, followingCount,postCount;
    private Button editProfileButton;
    private RecyclerView postsRecyclerView;
    private ProgressBar progressBar;
    private ProfilePostAdapter postAdapter;
    private List<PostModel> userPosts;
    private DatabaseReference usersRef, postsRef, followRef;
    private FirebaseUser currentUser;
    public ProfileFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profileImage);
        username = view.findViewById(R.id.username);
        postCount = view.findViewById(R.id.postCount);
        bio = view.findViewById(R.id.bio);
        followersCount = view.findViewById(R.id.followersCount);
        followingCount = view.findViewById(R.id.followingCount);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView1);
        progressBar = view.findViewById(R.id.progressBar);

        postsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        userPosts = new ArrayList<>();
        postAdapter = new ProfilePostAdapter(getContext(), userPosts);
        postsRecyclerView.setAdapter(postAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        // Load user profile data
        loadUserProfile();

        // Load user posts
        loadUserPosts();

        // Edit profile button action
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserProfile() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("username").getValue(String.class);
                    String profilePicUrl = snapshot.child("profileImage").getValue(String.class);
                    String bioText = snapshot.child("bio").getValue(String.class);
                    long followers = snapshot.child("followers").getChildrenCount();
                    long following = snapshot.child("following").getChildrenCount();

                    username.setText(name);
                    bio.setText(bioText);
                    followersCount.setText(String.valueOf(followers));
                    followingCount.setText(String.valueOf(following));

                    Glide.with(getContext()).load(profilePicUrl).placeholder(R.drawable.avtar).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Follow");

        followRef.child(currentUser.getUid()).child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followersCount.setText(snapshot.getChildrenCount()+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Fetch following count
        followRef.child(currentUser.getUid()).child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingCount.setText(snapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        loadPostCount();
    }
    private void loadUserPosts() {
        progressBar.setVisibility(View.VISIBLE);
        postsRef.orderByChild("userId").equalTo(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userPosts.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PostModel post = dataSnapshot.getValue(PostModel.class);
                    if (post != null) {
                        userPosts.add(0, post); // Newest posts first
                    }
                }
                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private void loadPostCount() {
        postsRef.orderByChild("userId").equalTo(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount(); // Get total posts count
                postCount.setText(count + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
