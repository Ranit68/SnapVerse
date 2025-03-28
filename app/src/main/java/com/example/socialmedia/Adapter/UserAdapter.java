package com.example.socialmedia.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmedia.Activity.ProfileActivity;
import com.example.socialmedia.Fragment.ProfileFragment;
import com.example.socialmedia.R;
import com.example.socialmedia.Model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<UserModel> userList;
    private FirebaseAuth auth;
    private DatabaseReference followRef;

    public UserAdapter(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
        auth = FirebaseAuth.getInstance();
        followRef = FirebaseDatabase.getInstance().getReference("Follow");
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        if (user == null || user.getUserId() == null)return;
        holder.username.setText(user.getUsername());
        Glide.with(context).load(user.getProfileImageUrl()).placeholder(R.drawable.avtar).into(holder.profileImage);

        // Follow / Unfollow Button Logic
        String currentUserId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        if (user.getUserId().equals(currentUserId)){
            holder.followButton.setVisibility(View.GONE);
        }else {
            holder.followButton.setVisibility(View.VISIBLE);

            userRef.child(currentUserId).child("following").child(user.getUserId()).get()
                    .addOnSuccessListener(dataSnapshot -> {
                        if (dataSnapshot.exists()){
                            holder.followButton.setText("Unfollow");
                        }else {
                            holder.followButton.setText("Follow");
                        }
                    });
            holder.followButton.setOnClickListener(v -> {
                DatabaseReference followingRef = userRef.child(currentUserId).child("following").child(user.getUserId());
                DatabaseReference followersRef = userRef.child(user.getUserId()).child("followers").child(currentUserId);

                followingRef.get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // Unfollow
                        followingRef.removeValue();
                        followersRef.removeValue();
                        holder.followButton.setText("Follow");
                    } else {
                        // Follow
                        followingRef.setValue(true);
                        followersRef.setValue(true);
                        holder.followButton.setText("Unfollow");
                    }
                });
            });
        }
//        String currentUserId = auth.getCurrentUser().getUid();
//        if (user.getUserId().equals(currentUserId)) {
//            holder.followButton.setVisibility(View.GONE); // Hide follow button for self
//        } else {
//            holder.followButton.setVisibility(View.VISIBLE);
//            holder.followButton.setOnClickListener(v -> {
//                followRef.child(currentUserId).child("Following").child(user.getUserId()).setValue(true);
//                followRef.child(user.getUserId()).child("Followers").child(currentUserId).setValue(true);
//            });
//        }

        // Open User Profile on Click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("userId", user.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        ImageView profileImage;
        Button followButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.usernameIt);
            profileImage = itemView.findViewById(R.id.profileImageIt);
            followButton = itemView.findViewById(R.id.followButtonIt);
        }
    }
}
