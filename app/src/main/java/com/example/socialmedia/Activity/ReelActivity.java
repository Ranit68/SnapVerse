package com.example.socialmedia.Activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.socialmedia.Model.PostModel;
import com.example.socialmedia.R;
import com.example.socialmedia.Adapter.ReelAdapter;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ReelActivity extends AppCompatActivity {
    private ViewPager2 reelViewPager;
    private List<PostModel> reelList;
    private ReelAdapter reelAdapter;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.socialmedia.R.layout.activity_reel);

        reelViewPager = findViewById(R.id.reelViewPager);
        reelList = new ArrayList<>();
        reelAdapter = new ReelAdapter(this, reelList);
        reelViewPager.setAdapter(reelAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        loadReels();
    }

    private void loadReels() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reelList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    PostModel post = ds.getValue(PostModel.class);
                    if (post != null && "video".equals(post.getMediaType())) { // Check if mediaType is "video"
                        reelList.add(post);
                    }
                }
                reelAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
