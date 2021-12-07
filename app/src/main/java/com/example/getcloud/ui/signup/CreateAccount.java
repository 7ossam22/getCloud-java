package com.example.getcloud.ui.signup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.getcloud.R;
import com.example.getcloud.ui.home.Home;
import com.example.getcloud.ui.login.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccount extends AppCompatActivity {
    EditText username_txt, email_txt, password_txt;
    Button signup_btn;
    FirebaseAuth auth;
    FirebaseUser user;
    ProgressBar progressBar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        progressBar = findViewById(R.id.progress_bar1);
        username_txt = findViewById(R.id.username);
        email_txt = findViewById(R.id.email);
        password_txt = findViewById(R.id.password);
        signup_btn = findViewById(R.id.signup);
        signup_btn.setOnClickListener(v -> {
            if (username_txt.getText().toString().isEmpty() && email_txt.getText().toString().isEmpty() && password_txt.getText().toString().isEmpty()) {
                Toast.makeText(CreateAccount.this, "Please fill Empty labels", Toast.LENGTH_LONG).show();
                username_txt.requestFocus();
            } else if (username_txt.getText().toString().isEmpty()) {
                username_txt.setError("Empty field");
                username_txt.requestFocus();
            } else if (email_txt.getText().toString().isEmpty()) {
                email_txt.setError("Empty field");
                email_txt.requestFocus();
            } else if (password_txt.getText().toString().isEmpty()) {
                password_txt.setError("Empty field");
                password_txt.requestFocus();
            } else {
                signup_btn.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                auth = FirebaseAuth.getInstance();
                auth.createUserWithEmailAndPassword(email_txt.getText().toString().trim(), password_txt.getText().toString().trim()).addOnCompleteListener(CreateAccount.this, task -> {
                    user = auth.getCurrentUser();
                    if (!task.isSuccessful()) {
                        Toast.makeText(CreateAccount.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        signup_btn.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    } else {
                        signup_btn.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        String UID = user.getUid();

                        //Pushing Data to db
                        Map<String, String> map = new HashMap<>();
                        map.put("Name", username_txt.getText().toString().trim());
                        map.put("Email", email_txt.getText().toString().trim());
                        map.put("Profile_Pic", "");
                        map.put("UID", UID);
                        map.put("Usage", "0");

                        db.collection("Users").document(UID).set(map).addOnCompleteListener(task1 -> {
                            if (!task1.isSuccessful()) {
                                Toast.makeText(CreateAccount.this, "Failed to push your Data ", Toast.LENGTH_LONG).show();
                            } else {
                                //Place Intent Here --->
                                Toast.makeText(CreateAccount.this, "Data Registered Successfuly", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(CreateAccount.this, Home.class).putExtra("UID", UID));
                                finish();
                            }
                        });
                    }
                });
            }
        });
        signup_btn.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    public void toMainActivity(View view) {
        startActivity(new Intent(CreateAccount.this, MainActivity.class));
        finish();
    }
}