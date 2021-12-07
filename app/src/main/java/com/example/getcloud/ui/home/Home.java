package com.example.getcloud.ui.home;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.getcloud.R;
import com.example.getcloud.pojo.File_Data;
import com.example.getcloud.ui.login.MainActivity;
import com.example.getcloud.ui.profile.Profile;
import com.example.getcloud.ui.upload.UploadData;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class Home extends AppCompatActivity {

    //Declaring any Layout attachments here -->

    TextView NO_DATA, sizeUsed;
    RecyclerView recyclerView;
    ProgressBar progressBar, storageCapacity;
    ImageView menu, USER_IMAGE;
    AlertDialog alertDialog;
    AlertDialog.Builder alertdialogbuilder;

    //Declaring any Library functions here -->

    DocumentReference reference;
    StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private Adapter adapter;
    private FirestoreRecyclerOptions<File_Data> options;
    DocumentReference USER_DATA;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    //Declaring any Variables here -->

    String UID;
    double PERCENTAGE;
    int storageSize = 5242880;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        UID = intent.getStringExtra("UID");

        recyclerView = findViewById(R.id.recyclerView1);
        NO_DATA = findViewById(R.id.item_annotation);
        progressBar = findViewById(R.id.progress_bar2);
        menu = findViewById(R.id.menu);
        USER_IMAGE = findViewById(R.id.userimg);
        sizeUsed = findViewById(R.id.size_used);
        storageCapacity = findViewById(R.id.storage_capacity);

        //storageCapacity.setProgress(0);
        
        preferences = getApplicationContext().getSharedPreferences("checkbox", 0);
        editor = preferences.edit();

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Data");
        reference = firebaseFirestore.collection("Users").document(UID).collection("Data").document();

        //Here is the place if u want to call onWelcomingUser Function Again --->

        options = new FirestoreRecyclerOptions.Builder<File_Data>().setQuery(firebaseFirestore.collection("Users").document(UID).collection("Data"), File_Data.class).build();
        adapter = new Adapter(options, Home.this, UID);
        recyclerView.setLayoutManager(new GridLayoutManager(Home.this, 2));
        recyclerView.setAdapter(adapter);

        //Declaring menu functionality

        menu.setOnClickListener(view -> {

            PopupMenu popupMenu = new PopupMenu(Home.this, menu);
            popupMenu.getMenuInflater().inflate(R.menu.home_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {

                switch (menuItem.getItemId()) {
                    //Case 1
                    case R.id.Logout:
                        alertdialogbuilder = new AlertDialog.Builder(Home.this);
                        alertdialogbuilder.setTitle("Logout Confirmation...");
                        alertdialogbuilder.setIcon(R.drawable.ic_baseline_exit);
                        alertdialogbuilder.setMessage("Are you sure you want to logout?!");
                        alertdialogbuilder.setCancelable(false);
                        alertdialogbuilder.setPositiveButton("Yes", (dialogInterface, i) -> {
                            editor.clear();
                            editor.apply();
                            startActivity(new Intent(Home.this, MainActivity.class));
                            finish();
                        });
                        alertdialogbuilder.setNegativeButton("No", (dialogInterface, i) -> alertdialogbuilder.setCancelable(true));
                        alertDialog = alertdialogbuilder.create();
                        alertDialog.show();

                        break;
                    //Case 2
                    case R.id.profile:
                        startActivity(new Intent(Home.this, Profile.class).putExtra("UID", UID));
                        break;
                    //Case 3
                    case R.id.about:
                        Toast.makeText(Home.this, "You Clicked to View About Activity", Toast.LENGTH_LONG).show();
                        break;
                }
                return true;
            });
            popupMenu.show();
        });

    }

    public void toUpload(View view) {
        startActivity(new Intent(Home.this, UploadData.class).putExtra("UID", UID));
    }

    public void onWelcomingUser() {
        USER_DATA = firebaseFirestore.collection("Users").document(UID);
        USER_DATA.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String Profile_Pic = documentSnapshot.getString("Profile_Pic");
                String USAGE = documentSnapshot.getString("Usage");
                int CAPACITY = Integer.parseInt(USAGE);
                double size_kb = CAPACITY / 1024;
                double size_mb = size_kb / 1024;
                double size_gb = size_mb / 1024;
                double size_tb = size_gb / 1024;
                PERCENTAGE = ((size_kb / storageSize) * 100);
                if (size_tb >= 1) {
                    sizeUsed.setText((int) size_tb + "TB used /5GB");
                } else if (size_gb >= 1) {
                    sizeUsed.setText((int) size_gb + "GB used /5GB");
                } else if (size_mb >= 1) {
                    sizeUsed.setText((int) size_mb + "MB used /5GB");
                } else if (size_kb >= 1) {
                    sizeUsed.setText((int) size_kb + "KB used /5GB");
                } else {
                    sizeUsed.setText((int) CAPACITY + "Bytes used /5GB");
                }

                storageCapacity.setProgress((int) PERCENTAGE);

                if (!Profile_Pic.equals("")) {
                    Glide.with(Home.this)
                            .load(Profile_Pic)
                            .circleCrop()
                            .into(USER_IMAGE);
                } else if (Profile_Pic.equals("")) {
                    USER_IMAGE.setImageResource(R.drawable.ic_person);
                }

            }
        }).addOnFailureListener(e -> Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onStart() {
        super.onStart();
        onWelcomingUser();
        adapter.startListening();
        progressBar.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter.getItemCount() == 0) {
                    storageCapacity.setProgress(0);
                    firebaseFirestore.collection("Users").document(UID).update("Usage", "0").addOnCompleteListener(task1 -> {
                    });
                    NO_DATA.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                } else {
                    storageCapacity.setProgress((int) PERCENTAGE);
                    NO_DATA.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, 200);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    Boolean pressed = false;

    @Override
    public void onBackPressed() {

        if (pressed) {
            super.onBackPressed();
            return;
        }
        pressed = true;
        Toast.makeText(Home.this, "Press again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pressed = false;
            }
        }, 2000);
    }
}
