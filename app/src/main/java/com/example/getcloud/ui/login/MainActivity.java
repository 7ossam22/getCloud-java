package com.example.getcloud.ui.login;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.getcloud.R;
import com.example.getcloud.ui.home.Home;
import com.example.getcloud.ui.signup.CreateAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    //Declaring any Layout attachments here -->
    EditText email_txt, password_txt;
    Button signin_btn;
    ProgressBar progressBar;
    CheckBox rememberme;
    TextView forgotpassword, createaccount, about;

    //Declaring any Library functions here -->
    FirebaseAuth auth;
    FirebaseUser user;

    //Declaring any Variables here -->
    String CHECK_BOX;
    String UID;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Adjust variable to its values
        progressBar = findViewById(R.id.progress_bar2);
        email_txt = findViewById(R.id.email);
        password_txt = findViewById(R.id.password);
        rememberme = findViewById(R.id.checkBox);
        forgotpassword = findViewById(R.id.reset);
        signin_btn = findViewById(R.id.signin);
        createaccount = findViewById(R.id.newaccount);
        about = findViewById(R.id.about);

        //Calling Shared prefrences here --->

        preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        CHECK_BOX = preferences.getString("remember", "");
        //Checking if there is shared preferences or not
        if (CHECK_BOX.equals("true")) {
            email_txt.setVisibility(View.INVISIBLE);
            password_txt.setVisibility(View.INVISIBLE);
            signin_btn.setVisibility(View.INVISIBLE);
            forgotpassword.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            rememberme.setVisibility(View.INVISIBLE);
            createaccount.setVisibility(View.INVISIBLE);
            about.setVisibility(View.INVISIBLE);
            String user_email = preferences.getString("Email", "");
            String user_password = preferences.getString("Password", "");
            onSigningin(user_email, user_password);
        }

        //sign in functionality when button is on clicked

        signin_btn.setOnClickListener(v -> {
            if (email_txt.getText().toString().isEmpty() && password_txt.getText().toString().isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill Empty labels", Toast.LENGTH_LONG).show();
                email_txt.requestFocus();
            } else if (email_txt.getText().toString().isEmpty()) {
                email_txt.setError("Empty field");
                email_txt.requestFocus();
            } else if (password_txt.getText().toString().isEmpty()) {
                password_txt.setError("Empty field");
                password_txt.requestFocus();
            } else {
                signin_btn.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                onSigningin(email_txt.getText().toString().trim(), password_txt.getText().toString().trim());
            }
        });

        rememberme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isChecked()) {
                if (email_txt.getText().toString().isEmpty() && password_txt.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill Empty labels", Toast.LENGTH_LONG).show();
                    email_txt.requestFocus();
                    rememberme.setChecked(false);
                } else if (email_txt.getText().toString().isEmpty()) {
                    email_txt.setError("Empty field");
                    email_txt.requestFocus();
                    rememberme.setChecked(false);
                } else if (password_txt.getText().toString().isEmpty()) {
                    password_txt.setError("Empty field");
                    password_txt.requestFocus();
                    rememberme.setChecked(false);
                } else {
                    rememberme.setChecked(true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "true");
                    editor.putString("Email", email_txt.getText().toString().trim());
                    editor.putString("Password", password_txt.getText().toString().trim());
                    editor.apply();
                }

            } else if (!buttonView.isChecked()) {
                editor = preferences.edit();
                editor.putString("remember", "false");
                editor.putString("Email", "");
                editor.putString("Password", "");
                editor.apply();
            }
        });

    }

    // Moving to Create account activity
    public void toCreateAccount(View view) {
        startActivity(new Intent(MainActivity.this, CreateAccount.class));
        finish();
    }

    public void onSigningin(String EMAIL, String PASSWORD) {
        auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(EMAIL, PASSWORD).addOnCompleteListener(MainActivity.this, task -> {
            user = auth.getCurrentUser();
            if (!task.isSuccessful()) {
                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                email_txt.setVisibility(View.VISIBLE);
                password_txt.setVisibility(View.VISIBLE);
                signin_btn.setVisibility(View.VISIBLE);
                forgotpassword.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                rememberme.setVisibility(View.VISIBLE);
                createaccount.setVisibility(View.VISIBLE);
                about.setVisibility(View.VISIBLE);
                editor = preferences.edit();
                editor.clear();
                editor.apply();
            } else {
                signin_btn.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                UID = user.getUid();
                Toast.makeText(MainActivity.this, "Logging in Successfuly", Toast.LENGTH_SHORT).show();

                //Place Intent Here --->
                startActivity(new Intent(MainActivity.this, Home.class).putExtra("UID", UID));
                finish();
            }
        });
    }

}