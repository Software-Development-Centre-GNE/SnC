package com.sdc.shareandcare;


import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sdc.shareandcare.databinding.ActivityUploadedMediaBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UploadedMediaActivity extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notes");
    ActivityUploadedMediaBinding binding;
    public static ArrayList<Post> postsArrayList = new ArrayList<>();
    private PostsAdapter postsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadedMediaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadActivity();
    }

    private void loadActivity() {

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        logExistingDBEntries();
    }


    private void logExistingDBEntries() {
        postsArrayList.clear();
        databaseReference = FirebaseDatabase.getInstance().getReference("notes");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {

                    Map<String, Object> map = (HashMap<String, Object>) childDataSnapshot.getValue();
                    if (map==null){
                        Toast.makeText(UploadedMediaActivity.this, "No Existing Media", Toast.LENGTH_SHORT).show();
                    }

                    Post post = new Post();
                    post.setUrl(map.get("url").toString());
                    try {
                        post.setNote(map.get("note").toString());
                    }catch (NullPointerException e){
                        post.setNote("No Note with this Media");
                    }

                    postsArrayList.add(post);
                }
                postsAdapter = new PostsAdapter(UploadedMediaActivity.this, postsArrayList);
                binding.recyclerView.setAdapter(postsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}