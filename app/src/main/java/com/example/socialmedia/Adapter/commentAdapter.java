package com.example.socialmedia.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmedia.Model.CommentModel;
import com.example.socialmedia.R;

import java.util.List;

public class commentAdapter extends RecyclerView.Adapter<commentAdapter.ViewHolder> {
    private Context context;
    private List<CommentModel> commentList;

    public commentAdapter(Context context, List<CommentModel> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentModel comment = commentList.get(position);
        holder.username.setText(comment.getUsername());
        holder.commentText.setText(comment.getCommentText());

        Glide.with(context).load(comment.getProfileImageUrl()).placeholder(R.drawable.avtar).into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, commentText;
        ImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.commentUsername);
            commentText = itemView.findViewById(R.id.commentText);
            profileImage = itemView.findViewById(R.id.commentProfileImage);
        }
    }
}
