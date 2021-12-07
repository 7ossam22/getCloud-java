package com.example.getcloud.ui.upload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.getcloud.R;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
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
import java.util.Map;

import static com.example.getcloud.R.drawable.choosebtn2_style;
import static com.example.getcloud.R.drawable.choosebtn_style;
import static com.google.firebase.storage.UploadTask.*;

public class UploadData extends AppCompatActivity {

    String UID;
    EditText FILE_NAME;
    TextView percentage, UPLOADING;
    ProgressBar progressBar1, progressBar2;
    Button CHOOSE_FILE, UPLOAD_FILE;
    String downloadLink;
    Map<String, String> map;
    public static Uri uri;
    FirebaseFirestore firebaseFirestore;
    DocumentReference reference;
    StorageReference storageReference;
    double progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_data);
        Intent intent = getIntent();
        UID = intent.getStringExtra("UID");

        UPLOADING = findViewById(R.id.uploading);
        percentage = findViewById(R.id.percent);
        CHOOSE_FILE = findViewById(R.id.choosefile);
        UPLOAD_FILE = findViewById(R.id.uploadfile);
        FILE_NAME = findViewById(R.id.filename);
        progressBar1 = findViewById(R.id.progress_bar2);
        progressBar2 = findViewById(R.id.progress_bar3);


        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Data").child(UID);
        reference = firebaseFirestore.collection("Users").document(UID).collection("Data").document();

        CHOOSE_FILE.setOnClickListener(v -> {
            CHOOSE_FILE.setTextColor(Color.rgb(86,190,227));
            selectingFile();

        });

        UPLOAD_FILE.setOnClickListener(v -> {
            UPLOAD_FILE.setVisibility(View.INVISIBLE);
            if (uri != null) {
                UPLOAD_FILE.setVisibility(View.INVISIBLE);
                uploadFile(uri);
            } else {
                UPLOAD_FILE.setVisibility(View.INVISIBLE);
                Toast.makeText(UploadData.this, "No file selected", Toast.LENGTH_LONG).show();
            }
        });

    }

    //function just to select file from storage
    private void selectingFile() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 86);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) {

            CHOOSE_FILE.setTextColor(Color.WHITE);
            uri = data.getData();
            String path = data.getData().getPath();
            int x = path.lastIndexOf('/');
            String name = path.substring((x + 1));
            FILE_NAME.setText(name);
        }
        else
        {
            CHOOSE_FILE.setTextColor(Color.WHITE);
            Toast.makeText(UploadData.this,"Failed to select your content",Toast.LENGTH_LONG).show();
        }
    }

    //function to upload data that we selected to firebase Storage
    private void uploadFile(final Uri uri) {

        if (uri.toString() != "" && FILE_NAME.getText().toString().trim() != "") {
            storageReference.child(FILE_NAME.getText().toString().trim()).putFile(uri).addOnSuccessListener(taskSnapshot -> {

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        percentage.setText("0%");
                        progressBar2.setProgress(0);
                        percentage.setVisibility(View.GONE);
                        progressBar1.setVisibility(View.GONE);
                        progressBar2.setVisibility(View.GONE);
                        UPLOADING.setVisibility(View.GONE);
                        UPLOAD_FILE.setVisibility(View.VISIBLE);
                    }
                }, 2000);

                storageReference.child(taskSnapshot.getMetadata().getName()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png'
                        downloadLink = uri.toString();

                        Date c = Calendar.getInstance().getTime();
                        System.out.println("Current time => " + c);
                        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
                        String formattedDate = df.format(c);

                        map = new HashMap<>();
                        map.put("Name", taskSnapshot.getMetadata().getName().trim());
                        map.put("link", downloadLink);
                        map.put("Type", taskSnapshot.getMetadata().getContentType());
                        map.put("Size", String.valueOf(taskSnapshot.getMetadata().getSizeBytes()));
                        map.put("Date", formattedDate);
                        reference.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(UploadData.this, "Data uploaded successfuly", Toast.LENGTH_LONG).show();
                                finish();
                                //onBackPressed();
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(UploadData.this, "Failed to add your Data" + downloadLink, Toast.LENGTH_LONG).show();
                            percentage.setVisibility(View.GONE);
                            progressBar1.setVisibility(View.GONE);
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Toast.makeText(UploadData.this, "Failed to add your Data" + downloadLink, Toast.LENGTH_LONG).show();
                        percentage.setVisibility(View.GONE);
                        progressBar1.setVisibility(View.GONE);
                        UPLOAD_FILE.setVisibility(View.VISIBLE);

                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Toast.makeText(UploadData.this,"Error while uploading you file",Toast.LENGTH_LONG).show();
                    }
                });


            }).addOnFailureListener(e -> Toast.makeText(UploadData.this, e.getMessage(), Toast.LENGTH_LONG).show()).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    UPLOADING.setVisibility(View.VISIBLE);
                    UPLOAD_FILE.setVisibility(View.INVISIBLE);
                    progress = (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    percentage.setVisibility(View.VISIBLE);
                    progressBar1.setVisibility(View.VISIBLE);
                    progressBar2.setVisibility(View.VISIBLE);
                    progressBar2.setProgress((int) progress);
                    percentage.setText((int) progress + "%");


                }
            });
        } else {
            Toast.makeText(UploadData.this, "No Data selected", Toast.LENGTH_SHORT).show();
        }

    }


    public void onBackPressed(View view) {
        super.onBackPressed();
    }
}