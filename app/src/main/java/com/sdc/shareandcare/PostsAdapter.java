package com.sdc.shareandcare;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Random;

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

        // Inflate the dialog layout file
        View dialogLayout = LayoutInflater.from(context).inflate(R.layout.dialog_add_description, null);
        builder.setView(dialogLayout);

        // Get a reference to the EditText in the dialog layout file
        EditText descriptionEditText = dialogLayout.findViewById(R.id.description_edit_text);
        //  input.setInputType(InputType.TYPE_CLASS_TEXT);
        descriptionEditText.setHint("Enter description here");
        descriptionEditText.setText(post.getDescription());
        // builder.setView(descriptionEditText);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String description = descriptionEditText.getText().toString();
                post.setDescription(description);
                saveDescriptionToFirebase(post);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();

        // Set the maximum width of the TextView in the dialog layout file
        //   int maxWidth = context.getResources().getDimensionPixelSize(R.dimen.dialog_max_width);
        TextView textView = dialogLayout.findViewById(R.id.description_text_view);
        textView.setMaxWidth(200);

        dialog.show();
    }

    @Override
    public void onBindViewHolder(@NonNull PostsAdapter.ViewHolder holder, int position) {
        final Post post = postsArrayList.get(position);

        holder.textView.setText(post.getNote());
        holder.imageView.setOnClickListener(v -> downloadFile(post));

        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDescriptionDialog(post);
            }
        });


    }
    private void downloadFile(Post post) {
        Log.v("down", post.getUrl());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("image/").child(post.getUrl());

        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.v("Down", uri.toString());
                String url = uri.toString();


                // Create an Intent to view the downloaded image
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(uri, "image/*");
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


                // Start the activity to view the downloaded image
//                try {
////                    context.startActivity(intent);
//                } catch (ActivityNotFoundException e) {
//                    // Handle the case where no app is available to view the image
//                    Log.e("DownloadError", "No app available to view image: " + e.getMessage());
//                }

//               Generating a random number for FileName
                String hexRandom = getRandomHexString(3);
                String Filename = "Snc_" + hexRandom;
//              method to download
                downloadFiles(context, Filename, DIRECTORY_DOWNLOADS, url);
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
        builder.setTitle("");
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

//    This method will download files by requesting a system download manager
    public void downloadFiles(Context context, String FileName, String destinationDirectory, String url ){
        DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, FileName);
        downloadManager.enqueue(request);

    }
    private String getRandomHexString(int range){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < range){
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, range);
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
        ImageView imageView;
        public View addButton;
        TextView textView;

        public ViewHolder(@NonNull View view){
            super(view);
            textView = view.findViewById(R.id.textView2);
            addButton = view.findViewById(R.id.imageButton);
            imageView = view.findViewById(R.id.imageView2);
        }
    }
    public PostsAdapter(Context context, ArrayList<Post> postsArrayList){
        this.context = context;
        this.postsArrayList = postsArrayList;
    }
}