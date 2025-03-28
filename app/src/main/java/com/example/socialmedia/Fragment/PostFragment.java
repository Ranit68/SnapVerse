package com.example.socialmedia.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.socialmedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.UUID;

public class PostFragment extends Fragment {

    private static final int PICK_MEDIA_REQUEST = 1;
    private Uri mediaUri;
    private ImageView imagePreview, selectMediaButton;
    private EditText captionInput, descriptionInput;
    private ProgressBar progressBar;
    private Button uploadButton ;
    private FirebaseAuth auth;
    private DatabaseReference postRef, userPostRef;
    private StorageReference storageRef;
    private String mediaType = "image";
    private static final int STORAGE_PERMISSION_CODE = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        imagePreview = view.findViewById(R.id.imagePreview);
        captionInput = view.findViewById(R.id.captionInput);
        descriptionInput = view.findViewById(R.id.descriptionInput);
        progressBar = view.findViewById(R.id.progressBar);
        uploadButton = view.findViewById(R.id.uploadButton);
        selectMediaButton = view.findViewById(R.id.selectMediaButton);

        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        postRef = FirebaseDatabase.getInstance().getReference("Posts");
        userPostRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Posts");
        storageRef = FirebaseStorage.getInstance().getReference("Posts");

        selectMediaButton.setOnClickListener(v -> openGallery());
        uploadButton.setOnClickListener(v -> uploadPost());

        return view;
    }

    private void openGallery() {
        if (checkStoragePermission()) {
            selectMedia(); // If permission is granted, open the gallery
        } else {
            requestStoragePermission(); // If not, ask for permission
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_MEDIA_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            mediaUri = data.getData();
            if (mediaUri != null) {
                String mimeType = getActivity().getContentResolver().getType(mediaUri);
                mediaType = (mimeType != null && mimeType.startsWith("video")) ? "video" : "image";
                imagePreview.setImageURI(mediaUri); // Show selected media
                Toast.makeText(getContext(), "Media selected successfully!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No media selected", Toast.LENGTH_SHORT).show();
            }
        }

    private void uploadPost() {
        String caption = captionInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (mediaUri == null) {
            Toast.makeText(getContext(), "Select a photo or video first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(caption)) {
            captionInput.setError("Caption required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        uploadButton.setEnabled(false);
        final String postId = UUID.randomUUID().toString();
        final StorageReference fileRef = storageRef.child(postId);

        fileRef.putFile(mediaUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String mediaUrl = uri.toString();
            String userId = auth.getCurrentUser().getUid();

            HashMap<String, Object> postMap = new HashMap<>();
            postMap.put("userId", userId);
            postMap.put("caption", caption);
            postMap.put("description", description);
            postMap.put("mediaUrl", mediaUrl);
            postMap.put("mediaType", mediaType);
            postMap.put("postId", postId);
            postMap.put("timestamp", System.currentTimeMillis());

            // Save post in global "Posts" and also in user's "Posts"
            postRef.child(postId).setValue(postMap);
            userPostRef.child(postId).setValue(postMap).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                uploadButton.setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Post uploaded!", Toast.LENGTH_SHORT).show();
                    resetUI();
                } else {
                    Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                }
            });

        })).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Permission Required")
                    .setMessage("This app needs access to your storage to select photos and videos.")
                    .setPositiveButton("OK", (dialog, which) -> requestPermissionsBasedOnAndroidVersion())
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            requestPermissionsBasedOnAndroidVersion();
        }
    }
    private void requestPermissionsBasedOnAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}, STORAGE_PERMISSION_CODE);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectMedia(); // Open gallery if permission is granted
            } else {
                Toast.makeText(getContext(), "Permission denied. Cannot select media.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void selectMedia() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_MEDIA_REQUEST);
    }

    private void resetUI() {
        captionInput.setText("");
        descriptionInput.setText("");
        imagePreview.setImageResource(0);
        mediaUri = null;
    }
}
