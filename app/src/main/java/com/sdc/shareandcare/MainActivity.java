package com.sdc.shareandcare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sdc.shareandcare.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Uri uri;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    ActivityMainBinding binding;
    ProgressDialog progressDialog;

    private static final int REQUEST_CODE_SELECT_MEDIA = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 103;
    private String mCurrentPhotoPath;
private Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonSelectMedia.setOnClickListener(v -> selectMedia("image/*"));
        binding.buttonSelectVideo.setOnClickListener(v -> selectMedia("video/*"));
        binding.buttonSelectAudio.setOnClickListener(v -> selectMedia("audio/*"));
        binding.buttonSelectCamera.setOnClickListener(v -> selectMedia("camera/*"));


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
                System.out.println("bitmapGallery"+ bitmap);
                imageView.setImageBitmap(bitmap);
                break;
            case "video/*":
                code = 101;
                binding.videoView.setVisibility(View.VISIBLE);
                MediaController mediaController = new MediaController(this);
                mediaController.setAnchorView(binding.videoView);
                binding.videoView.setMediaController(mediaController);
                break;
            case "audio/*":
                code = 102;
//                binding.audioView.setVisibility(View.VISIBLE);
//                MediaPlayer mediaPlayer = new MediaPlayer();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
//                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                            .setUsage(AudioAttributes.USAGE_MEDIA)
//                            .build());
//                }
//                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                    @Override
//                    public void onPrepared(MediaPlayer mp) {
//                        mp.start();
//                    }
//                });
//                try {
//                    Uri audioUri = data.getData();
//                    mediaPlayer.setDataSource(getApplicationContext(), audioUri);
//                    mediaPlayer.prepareAsync();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                break;
            case "camera/*":
                code = REQUEST_IMAGE_CAPTURE;
                break;
        }
        Intent intent;

        if (code == REQUEST_IMAGE_CAPTURE) {
            // Create an intent to capture an image
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    ex.printStackTrace();
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.android.fileprovider",
                            photoFile);
                    System.out.println("photoURI= "+  photoURI);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
        else{
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(mimeType);
//        String[] mimetypes = {"image/*", "video/*"};
//        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            startActivityForResult(intent, code);
        }


        binding.buttonSelectMedia.setVisibility(View.GONE);
        binding.buttonSelectVideo.setVisibility(View.GONE);
        binding.buttonSelectAudio.setVisibility(View.GONE);
        binding.buttonSelectCamera.setVisibility(View.GONE);


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
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        photoURI = FileProvider.getUriForFile(this,
                "com.example.android.fileprovider",
                imageFile);
//        mCurrentPhotoPath = imageFile;
        return imageFile;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(data !=null && data.getData() != null){
            uri = data.getData();
        System.out.println("uriGallery  "+ uri);
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
            else if(requestCode == 102 ){
                binding.buttonUploadMedia.setVisibility(View.VISIBLE);
                binding.editText.setVisibility(View.VISIBLE);
            }

        }

       else  if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Set the image as the Bitmap of the ImageView
//            content://com.example.android.fileprovider/external_files/Android/data/com.sdc.shareandcare/files/Pictures/JPEG_20230301_070604_7707464152446643116.jpg
//            content://com.android.providers.media.documents/document/image%3A498483

                System.out.println("photoURI    " + photoURI);
            // Convert URI to Bitmap
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
                System.out.println("bitmap Camera"+ bitmap);
                binding.imageView.setImageBitmap(bitmap);
//                binding.imageView.setImageURI(photoURI);
                binding.buttonUploadMedia.setVisibility(View.VISIBLE);
                binding.editText.setVisibility(View.VISIBLE);
                binding.buttonSelectMedia.setVisibility(View.GONE);
                binding.buttonSelectVideo.setVisibility(View.GONE);
                binding.buttonSelectAudio.setVisibility(View.GONE);
                binding.buttonSelectCamera.setVisibility(View.GONE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            binding.imageView.setImageURI(photoURI);
//
//            binding.buttonUploadMedia.setVisibility(View.VISIBLE);
//            binding.editText.setVisibility(View.VISIBLE);
//            binding.buttonSelectMedia.setVisibility(View.GONE);
//            binding.buttonSelectVideo.setVisibility(View.GONE);
//            binding.buttonSelectAudio.setVisibility(View.GONE);
//            binding.buttonSelectCamera.setVisibility(View.GONE);

        }
        else{
            binding.buttonSelectMedia.setVisibility(View.VISIBLE);
            binding.buttonSelectVideo.setVisibility(View.VISIBLE);
            binding.buttonSelectAudio.setVisibility(View.VISIBLE);
            binding.buttonSelectCamera.setVisibility(View.VISIBLE);
        }

    }

    private void uploadMedia() {

        if(photoURI != null){

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

                System.out.println("postttttt   " + post);

                databaseReference.child(String.valueOf(System.currentTimeMillis()/1000)).setValue(post);


                storageReference.putFile(photoURI).addOnSuccessListener(taskSnapshot -> {
//                    binding.imageView.setImageURI(null);
                    Toast.makeText(MainActivity.this,"File Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    hideAllViews();
                    progressDialog.dismiss();
                    binding.editText.setText("");
                }).addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this,"Failed to Upload",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
//                photoURI = null;
            }
            else{
                Toast.makeText(MainActivity.this,"Please Enter Title",Toast.LENGTH_SHORT).show();
            }
        }

        else  if(uri == null){
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


            storageReference.putFile(photoURI).addOnSuccessListener(taskSnapshot -> {
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
        binding.buttonSelectAudio.setVisibility(View.VISIBLE);
        binding.buttonSelectCamera.setVisibility(View.VISIBLE);
        binding.buttonUploadMedia.setVisibility(View.GONE);
        binding.editText.setVisibility(View.GONE);
        binding.videoView.setVisibility(View.GONE);
    }

}
