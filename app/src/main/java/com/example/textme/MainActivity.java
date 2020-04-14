package com.example.textme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private EditText userEmail, userName, userPassword;
    private Button btnSignUp, btnLogin;
    private DatabaseReference DBRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Sign-Up to TextMe");
        setActionBarColour();
        mAuth = FirebaseAuth.getInstance();
        DBRoot = FirebaseDatabase.getInstance().getReference();
        userEmail = findViewById(R.id.edtEmail);
        userName = findViewById(R.id.edtUserName);
        userPassword = findViewById(R.id.edtPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.btnLogIn);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(MainActivity.this , LoginActivity.class));

            }
        });
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Move to chat page
            startActivity( new Intent(MainActivity.this, ChatListActivity.class));

        }

    }

    private void signUp() {
        mAuth.createUserWithEmailAndPassword(userEmail.getText().toString(), userPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "createUserWithEmail:success", Toast.LENGTH_SHORT).show();

                            // Create a hash map and put details to the database under "user_details" under "the text_me_users"
                            HashMap<String, String> userDetails = new HashMap<>();
                            userDetails.put("userName", userName.getText().toString());
                            userDetails.put("createdDate", new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
                            userDetails.put("createdTime", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
                            DBRoot.child("text_me_users")
                                    .child(task.getResult().getUser().getUid())
                                    .child("user_details").push().setValue(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "User details added to database", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            // Add user names to "all_users" under "the text_me_users"
                            DBRoot
                                    .child("text_me_users")
                                    .child("all_users")
                                    .child(task.getResult().getUser().getUid()).setValue(userName.getText().toString());
                            //.push().setValue(userName.getText().toString());
                            updateUserProfile();

                            // Move to chat page
                            startActivity( new Intent(MainActivity.this, ChatListActivity.class));

                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUserProfile() {

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName.getText().toString())
                .build();
        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Display name updatetd", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setActionBarColour() {
        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#0a2876"));
        actionBar.setBackgroundDrawable(colorDrawable);
    }

}
