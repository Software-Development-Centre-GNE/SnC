package com.sdc.shareandcare;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder>  {

    private final Context context;
    private final ArrayList<Post> postsArrayList;


    @NonNull
    @Override
    public PostsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostsAdapter.ViewHolder holder, int position) {
        final Post post = postsArrayList.get(position);

        holder.textView.setText(post.getNote());

        holder.itemView.setOnClickListener(v -> downloadFile(post));

    }
    private void downloadFile(Post post) {
        Log.v("down", post.getUrl());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("image/").child(post.getUrl());

        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.v("Down", uri.toString());

                // Create an Intent to view the downloaded image
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "image/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Start the activity to view the downloaded image
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Handle the case where no app is available to view the image
                    Log.e("DownloadError", "No app available to view image: " + e.getMessage());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.e("DownloadError", exception.getMessage());
            }
        });
    }




    private void openMediaInWebView(Uri uri) {
        Intent i = new Intent(context, WebViewActivity.class);
        i.putExtra("uri",uri);
        context.startActivity(i);
        // Zoom feature will be implemented.
    }

    @Override
    public int getItemCount() {
        return postsArrayList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View view){
            super(view);
            textView = view.findViewById(R.id.textView2);
        }
    }
    public PostsAdapter(Context context, ArrayList<Post> postsArrayList){
        this.context = context;
        this.postsArrayList = postsArrayList;
    }
}