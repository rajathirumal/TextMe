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

public class LoginActivity extends AppCompatActivity {

    private EditText email_login, password_login;
    private Button btnlogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Sign in to TextMe");
        setActionBarColour();
        email_login = findViewById(R.id.email_login);
        password_login = findViewById(R.id.password_login);
        btnlogin = findViewById(R.id.btnlogin_login);

        mAuth = FirebaseAuth.getInstance();
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        mAuth.signInWithEmailAndPassword(email_login.getText().toString(), password_login.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Sign-in successful", Toast.LENGTH_LONG).show();
                    startActivity( new Intent(LoginActivity.this, ChatListActivity.class));

                } else {
                    Toast.makeText(LoginActivity.this, "Sign-in failed", Toast.LENGTH_LONG).show();
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
