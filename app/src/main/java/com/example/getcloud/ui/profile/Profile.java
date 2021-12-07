package com.example.getcloud.ui.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.getcloud.R;
import com.example.getcloud.ui.home.Home;
import com.example.getcloud.ui.upload.UploadData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Profile extends AppCompatActivity {
    DocumentReference USER_DATA;
    private FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;

    String name;
    String UID;
    String downloadLink;
    public static Uri uri;

    ImageView USER_IMAGE;
    TextView USER_NAME, USER_EMAIL, CHANGE_PROFILEPIC;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        UID = intent.getStringExtra("UID");

        USER_IMAGE = findViewById(R.id.user_img);
        USER_NAME = findViewById(R.id.userName);
        USER_EMAIL = findViewById(R.id.userAccount);

        CHANGE_PROFILEPIC = findViewById(R.id.changeprofile);
        progressBar = findViewById(R.id.progress_bar4);

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Data").child(UID);
        USER_DATA = firebaseFirestore.collection("Users").document(UID);
        USER_DATA.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String name = documentSnapshot.getString("Name");
                String email = documentSnapshot.getString("Email");
                String Profile_Pic = documentSnapshot.getString("Profile_Pic");

                USER_NAME.setText(name);
                USER_EMAIL.setText(email);
                if (!Profile_Pic.equals("")) {
                    Glide.with(Profile.this)
                            .load(Profile_Pic)
                            .circleCrop()
                            .into(USER_IMAGE);
                } else if (Profile_Pic.equals("")) {
                    USER_IMAGE.setImageResource(R.drawable.ic_person);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Profile.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        CHANGE_PROFILEPIC.setOnClickListener(v -> {
            selectingFile();
        });
    }


    public void onBackPressed(View view) {
        super.onBackPressed();
    }

    private void selectingFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 86);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) {
            uri = data.getData();
            String path = data.getData().getPath();
            int x = path.lastIndexOf('/');
            name = path.substring((x + 1));
            uploadFile(uri);
            /*UPLOAD_PIC.setVisibility(View.VISIBLE);*/
        }
    }

    private void uploadFile(final Uri uri) {

        if (uri.toString() != "" && !name.equals("")) {
            storageReference.child("Profile picture").putFile(uri).addOnSuccessListener(taskSnapshot -> {
                storageReference.child(taskSnapshot.getMetadata().getName()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png'
                        downloadLink = uri.toString();
                        firebaseFirestore.collection("Users").document(UID).update("Profile_Pic", downloadLink).addOnCompleteListener(task1 -> {
                            if (!task1.isSuccessful()) {
                                Toast.makeText(Profile.this, "Failed to push your Data ", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(Profile.this, "Profile Updated", Toast.LENGTH_LONG).show();
                                finish();
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        progressBar.setVisibility(View.GONE);

                    }
                });

            }).addOnFailureListener(e -> Toast.makeText(Profile.this, e.getMessage(), Toast.LENGTH_LONG).show()).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        } else {

            Toast.makeText(Profile.this, "not picture selected", Toast.LENGTH_LONG).show();
        }

    }

}
