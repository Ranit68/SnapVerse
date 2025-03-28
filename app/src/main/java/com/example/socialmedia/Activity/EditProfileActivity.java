package com.example.socialmedia.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.socialmedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {
    private ImageView profileImage;
    private EditText usernameEditText, bioEditText;
    private Button saveButton;
    private Uri imageUri;
    private String currentProfileImageUrl;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileImage = findViewById(R.id.profileImage);
        usernameEditText = findViewById(R.id.usernameEditText);
        bioEditText = findViewById(R.id.bioEditText);
        saveButton = findViewById(R.id.saveButton);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("profileImageUrl");

        // Load existing data from ProfileFragment (if available)
        Intent intent = getIntent();
        if (intent != null) {
            usernameEditText.setText(intent.getStringExtra("username"));
            bioEditText.setText(intent.getStringExtra("bio"));
            currentProfileImageUrl = intent.getStringExtra("profileImageUrl");

            if (currentProfileImageUrl != null) {
                Glide.with(this).load(currentProfileImageUrl).placeholder(R.drawable.avtar).into(profileImage);
            }
        }

        // Handle profile image selection
        profileImage.setOnClickListener(v -> openImagePicker());

        // Save profile updates
        saveButton.setOnClickListener(v -> updateUserProfile());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void updateUserProfile() {
        progressDialog.show();

        String username = usernameEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();

        if (username.isEmpty()) {
            usernameEditText.setError("Username cannot be empty");
            progressDialog.dismiss();
            return;
        }

        if (imageUri != null) {
            // Upload new profile image
            StorageReference fileRef = storageRef.child(currentUser.getUid() + ".jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateDatabase(username, bio, uri.toString());
                    })
            ).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Update only text data if no new image is selected
            updateDatabase(username, bio, currentProfileImageUrl);
        }
    }

    private void updateDatabase(String username, String bio, String imageUrl) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("bio", bio);
        updates.put("profileImageUrl", imageUrl);

        usersRef.updateChildren(updates).addOnSuccessListener(unused -> {
            progressDialog.dismiss();
            Toast.makeText(EditProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity after saving
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(EditProfileActivity.this, "Failed to update profile!", Toast.LENGTH_SHORT).show();
        });
    }
}
