package com.example.socialmedia.Adapter;

import android.content.Context;
import android.media.Image;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmedia.Model.PostModel;
import com.example.socialmedia.R;

import java.util.List;

public class ReelAdapter extends RecyclerView.Adapter<ReelAdapter.ReelViewHolder> {
    private final Context context;
    private final List<PostModel> reelList;

    public ReelAdapter(Context context, List<PostModel> reelList) {
        this.context = context;
        this.reelList = reelList;
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reel, parent, false);
        return new ReelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReelViewHolder holder, int position) {
        PostModel reel = reelList.get(position);
        holder.reelVideo.setVideoPath(reel.getMediaUrl());
        holder.reelVideo.setOnPreparedListener(mp -> {
            holder.mediaPlayer = mp;
            holder.mediaPlayer.setVolume(0f, 0f);
            mp.start();
        });
        holder.muteBtn.setImageResource(R.drawable.mute);
        holder.muteBtn.setOnClickListener(v -> toggleMute(holder));
    }
    private void toggleMute(ReelViewHolder holder) {
        if (holder.mediaPlayer != null){
            if (holder.isMuted){
                holder.mediaPlayer.setVolume(1f,1f);
                holder.muteBtn.setImageResource(R.drawable.volume);
            }else {
                holder.mediaPlayer.setVolume(0f,0f);
                holder.muteBtn.setImageResource(R.drawable.mute);
            }
            holder.isMuted = !holder.isMuted;
        }
    }

    @Override
    public int getItemCount() {
        return reelList.size();
    }

    public static class ReelViewHolder extends RecyclerView.ViewHolder {
        VideoView reelVideo;
        ImageView muteBtn;
        MediaPlayer mediaPlayer;
        boolean isMuted = true;

        public ReelViewHolder(@NonNull View itemView) {
            super(itemView);
            reelVideo = itemView.findViewById(R.id.reelVideoView);
            muteBtn = itemView.findViewById(R.id.muteButton);
        }
    }
}
