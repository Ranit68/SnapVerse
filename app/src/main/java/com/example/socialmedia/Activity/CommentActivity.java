package com.example.socialmedia.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmedia.Model.CommentModel;
import com.example.socialmedia.R;
import com.example.socialmedia.Adapter.commentAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText commentInput;
    private ImageView sendButton, profileImage;
    private String postId, currentUserId;
    private DatabaseReference commentsRef, userRef;
    private List<CommentModel> commentList;
    private com.example.socialmedia.Adapter.commentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        commentInput = findViewById(R.id.commentInput);
        sendButton = findViewById(R.id.sendButton);
        profileImage = findViewById(R.id.profileImage);

        // Get postId and current user ID
        postId = getIntent().getStringExtra("postId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Firebase references
        commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();
        commentAdapter = new commentAdapter(this, commentList);
        recyclerView.setAdapter(commentAdapter);

        // Load user profile and comments
        loadUserProfile();
        loadComments();

        // Send comment action
        sendButton.setOnClickListener(v -> addComment());
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("profileImageUrl").getValue() != null) {
                    String profileUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    Glide.with(CommentActivity.this).load(profileUrl).placeholder(R.drawable.avtar).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComments() {
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CommentModel comment = dataSnapshot.getValue(CommentModel.class);
                    if (comment != null) {
                        commentList.add(comment);
                    }
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentActivity.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addComment() {
        String commentText = commentInput.getText().toString().trim();
        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "Comment cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentId = commentsRef.push().getKey();
        if (commentId == null) return;

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    HashMap<String, Object> commentData = new HashMap<>();
                    commentData.put("commentId", commentId);
                    commentData.put("postId", postId);
                    commentData.put("userId", currentUserId);
                    commentData.put("commentText", commentText);
                    commentData.put("timestamp", System.currentTimeMillis());
                    commentData.put("username", snapshot.child("username").getValue(String.class));
                    commentData.put("profileImageUrl", snapshot.child("profileImageUrl").getValue(String.class));

                    commentsRef.child(commentId).setValue(commentData).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            commentInput.setText("");
                        } else {
                            Toast.makeText(CommentActivity.this, "Failed to add comment", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
