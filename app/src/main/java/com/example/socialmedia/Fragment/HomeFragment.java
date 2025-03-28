package com.example.socialmedia.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmedia.Adapter.PostAdapter;
import com.example.socialmedia.Model.PostModel;
import com.example.socialmedia.R;
import com.example.socialmedia.Adapter.StoryAdapter;
import com.example.socialmedia.Model.StoryModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private ImageView chat;
    private List<PostModel> postList;
    private ProgressBar progressBar;
    private DatabaseReference postsRef;

    private RecyclerView storyRecyclerView;
    private StoryAdapter storyAdapter;
    private List<StoryModel> storyList;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private FloatingActionButton uploadStoryButton;

    public HomeFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        storyRecyclerView = view.findViewById(R.id.storyRecyclerView);
        uploadStoryButton = view.findViewById(R.id.uploadStoryButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(postAdapter);

        storyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), storyList);
        storyRecyclerView.setAdapter(storyAdapter);

        // Initialize Firebase reference
        postsRef = FirebaseDatabase.getInstance().getReference("Posts");

        uploadStoryButton.setOnClickListener(v -> openGallery());

        // Load data
        loadPosts();
        loadStories();
        deleteOldStories();

        return view;
    }

    private void loadPosts() {
        progressBar.setVisibility(View.VISIBLE);
        postsRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PostModel post = dataSnapshot.getValue(PostModel.class);
                    if (post != null) {
                        postList.add(0, post);
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

    private void loadStories() {
        DatabaseReference storyRef = FirebaseDatabase.getInstance().getReference("Stories");

        storyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyList.clear();
                for (DataSnapshot userStorySnapshot : snapshot.getChildren()) {
                    for (DataSnapshot storySnapshot : userStorySnapshot.getChildren()) {
                        StoryModel story = storySnapshot.getValue(StoryModel.class);

                        if (story != null && story.getMediaUrl() != null && story.getUserId() != null) {
                            Log.d("StoryLoad", "Loaded Story: " + story.getMediaUrl());
                            storyList.add(story);
                        } else {
                            Log.e("StoryLoad", "Skipping story due to missing data.");
                        }
                    }
                }

                if (storyList.isEmpty()) {
                    Log.e("StoryLoad", "No valid stories found in Firebase!");
                }

                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StoryLoad", "Failed to load stories: " + error.getMessage());
            }
        });
    }

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            if (imageUri != null) {
                                Log.d("StoryUpload", "Image selected: " + imageUri.toString());
                                uploadStory(imageUri);
                            } else {
                                Log.e("StoryUpload", "No image selected!");
                                Toast.makeText(getContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("StoryUpload", "Image selection canceled!");
                            Toast.makeText(getContext(), "Image selection canceled!", Toast.LENGTH_SHORT).show();
                        }
                    });

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pickImageLauncher.launch(intent);
    }


    private void uploadStory(Uri uri) {
        if (uri == null) return;

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String storyId = FirebaseDatabase.getInstance().getReference("Stories").push().getKey();

        if (storyId == null) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Error creating story ID!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fix: Ensure each story has a unique filename
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("Stories").child(userId).child(storyId + ".jpg");

        storageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            long timestamp = System.currentTimeMillis();

                            StoryModel story = new StoryModel(storyId, userId, downloadUri.toString(), "image", timestamp);
                            FirebaseDatabase.getInstance().getReference("Stories")
                                    .child(userId).child(storyId).setValue(story)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Story Uploaded!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "Failed to upload story!", Toast.LENGTH_SHORT).show();
                                        }
                                        Log.d("StoryUpload", "Uploading story: " + uri.toString());

                                        progressDialog.dismiss();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed to get image URL!", Toast.LENGTH_SHORT).show();
                        })
                ).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void deleteOldStories() {
        DatabaseReference storyRef = FirebaseDatabase.getInstance().getReference("Stories");
        long currentTime = System.currentTimeMillis();
        long twentyFourHours = 24 * 60 * 60 * 1000;

        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userStory : snapshot.getChildren()) {
                    for (DataSnapshot storySnapshot : userStory.getChildren()) {
                        StoryModel story = storySnapshot.getValue(StoryModel.class);
                        if (story != null && currentTime - story.getTimestamp() >= twentyFourHours) {
                            storySnapshot.getRef().removeValue();
                            Log.d("StoryDelete", "Deleted expired story: " + story.getStoryId());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StoryDelete", "Error deleting old stories: " + error.getMessage());
            }
        });
    }
}
