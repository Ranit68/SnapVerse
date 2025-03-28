package com.example.socialmedia.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmedia.Activity.MainActivity;
import com.example.socialmedia.R;
import com.example.socialmedia.Adapter.UserAdapter;
import com.example.socialmedia.Model.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import java.util.ArrayList;
import java.util.List;
public class SearchFragment extends Fragment {
    private EditText searchEditText;
    private ImageView back;
    private RecyclerView searchRecyclerView;
    private UserAdapter userAdapter;
    private List<UserModel> userList;
    private DatabaseReference usersRef;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        searchEditText = view.findViewById(R.id.searchEditText);
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), userList);
        searchRecyclerView.setAdapter(userAdapter);
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUsers(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        return view;
    }
    private void searchUsers(String query) {
        Query searchQuery = usersRef.orderByChild("username").startAt(query).endAt(query + "\uf8ff");
        searchQuery.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    UserModel user = data.getValue(UserModel.class);
                    if (user != null && user.getUsername() != null){
                        userList.add(user);
                    }

                }
                userAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }
}
