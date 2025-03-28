package com.example.socialmedia.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmedia.Model.PostModel;
import com.example.socialmedia.R;

import java.util.List;

public class ProfilePostAdapter extends RecyclerView.Adapter<ProfilePostAdapter.ProfilePostViewHolder> {
    private Context context;
    private List<PostModel> postList;

    public ProfilePostAdapter(Context context, List<PostModel> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public ProfilePostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile_post, parent, false);
        return new ProfilePostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilePostViewHolder holder, int position) {
        PostModel post = postList.get(position);

        // Load post image/video thumbnail
        Glide.with(context).load(post.getMediaUrl()).into(holder.profilePostImage);

        // Open post in full screen when clicked
//        holder.profilePostImage.setOnClickListener(v -> {
//            Intent intent = new Intent(context, PostDetailActivity.class);
//            intent.putExtra("postId", post.getPostId());
//            context.startActivity(intent);
//        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class ProfilePostViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePostImage;

        public ProfilePostViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePostImage = itemView.findViewById(R.id.profilePostImage);
        }
    }
}
