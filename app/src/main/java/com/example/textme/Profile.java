package com.example.textme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Profile extends AppCompatActivity implements View.OnClickListener {

    private TextView username_profile;
    private EditText gender_profile, birthday_profile;
    private Button btnUpdateUserData, btnAddFriends;
    private FirebaseAuth firebaseAuth;

    private DatabaseReference DBRoot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("You'r Profile");
        setActionBarColour();
        firebaseAuth = FirebaseAuth.getInstance();
        DBRoot = FirebaseDatabase.getInstance().getReference();

        username_profile = findViewById(R.id.username_profile);
        gender_profile = findViewById(R.id.edtGender_profile);
        birthday_profile = findViewById(R.id.edtBirthday_profile);


        btnUpdateUserData = findViewById(R.id.btnUpdateData);
        btnUpdateUserData.setOnClickListener(Profile.this);

        getDataFromDatabase();
    }

    private void getDataFromDatabase() {
        DBRoot.child("text_me_users")
                .child(firebaseAuth.getCurrentUser().getUid())
                .child("user_details")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        for (DataSnapshot ss : dataSnapshot.getChildren()) {

                            switch (ss.getKey()) {
                                case "userName":
                                    String currentUsername = (String) dataSnapshot.child(ss.getKey()).getValue();
                                    username_profile.setText(currentUsername);
                                    break;
                                case "gender":
                                    String currentUserGender = (String) dataSnapshot.child(ss.getKey()).getValue();
                                    gender_profile.setText(currentUserGender);
                                    break;
                                case "DOB":
                                    String currentUserDOB = (String) dataSnapshot.child("DOB").getValue();
                                    birthday_profile.setText(currentUserDOB);
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnUpdateData:
                if (!gender_profile.getText().toString().equals("") && !birthday_profile.getText().toString().equals("")) {
                    DBRoot.child("text_me_users")
                            .child(firebaseAuth.getCurrentUser().getUid())
                            .child("user_details")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        snapshot.getRef().child("gender").setValue(gender_profile.getText().toString());
                                        snapshot.getRef().child("DOB").setValue(birthday_profile.getText().toString());
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    throw databaseError.toException();
                                }
                            });
                }
                Toast.makeText(Profile.this, "update clicked ", Toast.LENGTH_LONG).show();
                break;


        }
    }

    private void setActionBarColour() {
        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#0a2876"));
        actionBar.setBackgroundDrawable(colorDrawable);
    }
}
