package com.sdc.shareandcare;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private void saveDescriptionToFirebase(Post post) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("posts").child(post.getUrl());
        databaseRef.child("description").setValue(post.getDescription())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Firebase", "Description saved to Firebase");
                        // show a message to the user to indicate that the description has been saved
                        Toast.makeText(context, "Description saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "Error saving description to Firebase: " + e.getMessage());
                        // show a message to the user to indicate that there was an error saving the description
                        Toast.makeText(context, "Error saving description", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Assume the Post object is passed to this Activity or Fragment as an argument called "post"
    private void showAddDescriptionDialog(Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Description");

        // Set up the input field
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter description here");
        input.setText(post.getDescription());
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String description = input.getText().toString();
                post.setDescription(description);
                saveDescriptionToFirebase(post);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog box
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onBindViewHolder(@NonNull PostsAdapter.ViewHolder holder, int position) {
        final Post post = postsArrayList.get(position);

        holder.textView.setText(post.getNote());
        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDescriptionDialog(post);
            }
        });
       // Scanner sc = new Scanner(System.in);
        //String description = " ";
        //holder.itemView.setOnClickListener(v -> showDescriptionDialog(post.setDescription(description)));

        //holder.itemView.setOnClickListener(v -> downloadFile(post));

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
    private void showDescriptionDialog(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Description");
        builder.setMessage(description);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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
        public View addButton;
        TextView textView;

        public ViewHolder(@NonNull View view){
            super(view);
            textView = view.findViewById(R.id.textView2);
            addButton = view.findViewById(R.id.imageView2);
        }
    }
    public PostsAdapter(Context context, ArrayList<Post> postsArrayList){
        this.context = context;
        this.postsArrayList = postsArrayList;
    }
}