package com.sdc.shareandcare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sdc.shareandcare.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Uri uri;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    ActivityMainBinding binding;
    ProgressDialog progressDialog;

    private static final int REQUEST_CODE_SELECT_MEDIA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonSelectMedia.setOnClickListener(v -> selectMedia("image/*"));
        binding.buttonSelectVideo.setOnClickListener(v -> selectMedia("video/*"));


        binding.buttonUploadMedia.setOnClickListener(v -> uploadMedia());
        progressDialog = new ProgressDialog(this);

        binding.viewAll.setOnClickListener(v -> activityShowAll());
    }


    private void activityShowAll() {
        Intent i = new Intent(this, UploadedMediaActivity.class);
        startActivity(i);
    }

    private void selectMedia(String mimeType) {
        System.out.println("I am mimeType " + mimeType);
        int code = 0;
        switch (mimeType){
            case "image/*":
                code = 100;
                binding.imageView.setVisibility(View.VISIBLE);
                ImageView imageView = findViewById(R.id.imageView);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.your_image);
                imageView.setImageBitmap(bitmap);
                break;
            case "video/*":
                code = 101;
                binding.videoView.setVisibility(View.VISIBLE);
                MediaController mediaController = new MediaController(this);
                mediaController.setAnchorView(binding.videoView);
                binding.videoView.setMediaController(mediaController);
                break;
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
//        String[] mimetypes = {"image/*", "video/*"};
//        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        startActivityForResult(intent, code);
        binding.buttonSelectMedia.setVisibility(View.GONE);
        binding.buttonSelectVideo.setVisibility(View.GONE);


//        binding.imageView.setVisibility(View.VISIBLE);
//        ImageView imageView = findViewById(R.id.imageView);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.your_image);
//        imageView.setImageBitmap(bitmap);
//
//        Intent intent = new Intent();
//        intent.setType(mimeType);
//        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        startActivityForResult(Intent.createChooser(intent, "Select Media"), code);
//        binding.buttonSelectMedia.setVisibility(View.GONE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(data !=null && data.getData() != null){
            uri = data.getData();
            if(requestCode == 100) {
                binding.imageView.setImageURI(uri);

                binding.buttonUploadMedia.setVisibility(View.VISIBLE);
                binding.editText.setVisibility(View.VISIBLE);
            }
            else if(requestCode == 101){
                binding.videoView.setVideoURI(uri);
                binding.buttonUploadMedia.setVisibility(View.VISIBLE);
                binding.editText.setVisibility(View.VISIBLE);
            }
        }
        else{
            binding.buttonSelectMedia.setVisibility(View.VISIBLE);
            binding.buttonSelectVideo.setVisibility(View.VISIBLE);
        }

    }

    private void uploadMedia() {

        if(uri == null){
            Toast.makeText(this, "Please Select a File to Upload", Toast.LENGTH_SHORT).show();
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            finish();
            startActivity(getIntent());
            return;
        }

//
        EditText editText = findViewById(R.id.editText);
        String text = editText.getText().toString().trim();
        if(text.length() != 0){
            progressDialog.setTitle("Uploading");
            progressDialog.setMessage("Please wait while we upload the selected media");
            progressDialog.show();

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            Date currentDate = new Date();
            String fileName = format.format(currentDate);

            storageReference = FirebaseStorage.getInstance().getReference("image/"+fileName);
            databaseReference = FirebaseDatabase.getInstance().getReference("notes");

            Post post = new Post();
            post.setNote(binding.editText.getText().toString());
            post.setUrl(fileName);

            databaseReference.child(String.valueOf(System.currentTimeMillis()/1000)).setValue(post);


            storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                binding.imageView.setImageURI(null);
                Toast.makeText(MainActivity.this,"File Uploaded Successfully", Toast.LENGTH_SHORT).show();
                hideAllViews();
                progressDialog.dismiss();
                binding.editText.setText("");
            }).addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this,"Failed to Upload",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });
            uri = null;
        }
        else{
            Toast.makeText(MainActivity.this,"Please Enter Title",Toast.LENGTH_SHORT).show();
        }
    }

    private void hideAllViews(){
        binding.imageView.setVisibility(View.GONE);
        binding.buttonSelectMedia.setVisibility(View.VISIBLE);
        binding.buttonSelectVideo.setVisibility(View.VISIBLE);
        binding.buttonUploadMedia.setVisibility(View.GONE);
        binding.editText.setVisibility(View.GONE);
        binding.videoView.setVisibility(View.GONE);
    }

}
