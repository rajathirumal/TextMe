package com.example.textme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class AddFriends extends AppCompatActivity implements AdapterView.OnItemClickListener {


    private ListView allUsersListView;
    private static ArrayList<String> allUsersList;
    private ArrayAdapter allUsersAdapter;

    private FirebaseAuth firebaseAuth;
    private String selectedOtherUser;

    private ListView myFriendsListView;
    private static ArrayList<String> myFriendsList;
    private ArrayAdapter myFriendsAdapter;

    private HashMap<String, String> allUsersAndUID;
    private DatabaseReference DBRoot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        setTitle("Add friends");
        setActionBarColour();

        firebaseAuth = FirebaseAuth.getInstance();
        DBRoot = FirebaseDatabase.getInstance().getReference();

        allUsersListView = findViewById(R.id.lstAllUsers);
        allUsersList = new ArrayList<>();
        allUsersAdapter = new ArrayAdapter(AddFriends.this, android.R.layout.simple_list_item_1, allUsersList);
        allUsersListView.setAdapter(allUsersAdapter);
        allUsersListView.setOnItemClickListener(AddFriends.this);

        myFriendsListView = findViewById(R.id.lstMyFriends);
        myFriendsList = new ArrayList<>();
        myFriendsAdapter = new ArrayAdapter(AddFriends.this, android.R.layout.simple_list_item_1, myFriendsList);
        myFriendsListView.setAdapter(myFriendsAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAllUsersToRequestAsFriend();
        fetchRequestAcceptedFriends();
    }


    private void fetchRequestAcceptedFriends() {
        DBRoot.child("text_me_users")
                .child(firebaseAuth.getCurrentUser().getUid())
                .child("my_friends")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            //Log.i("usersss", "My friends " + d.getValue());
                            myFriendsList.add((String) d.getValue());
                            myFriendsAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void fetchAllUsersToRequestAsFriend() {
        allUsersAndUID = new HashMap<>();
        DBRoot.child("text_me_users")
                .child("all_users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            Log.i("all all user", d.getValue().toString());
                            Log.i("all current user ", Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName()));

                            // Ignore current use name from showing in the Other users list view
                            if (!FirebaseAuth.getInstance().getCurrentUser().getDisplayName().equals(d.getValue().toString())) {
                                allUsersList.add((String) d.getValue());
                                // Do not display friends in the other users list
                                DBRoot.child("text_me_users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("my_friends")
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                                    allUsersList.remove(data.getValue());
                                                    allUsersAdapter.notifyDataSetChanged();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        });
                                allUsersAndUID.put((String) d.getValue(), d.getKey());
                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }


    // Select user from "Other Users" -> id: "lstAllUsers"
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedOtherUser = allUsersList.get(position);

        // if the user has already requested to follow
        DBRoot.child("text_me_users")
                .child(firebaseAuth.getCurrentUser().getUid())
                .child("friend_request")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            if (Objects.requireNonNull(d.getValue()).toString().equals(selectedOtherUser)) {
                                ChatDataSharing.requestAlreadySent = true;
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

        if (!ChatDataSharing.requestAlreadySent) {
            // else
            // Check build version information.
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(AddFriends.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(AddFriends.this);
            }

            // prompt for confirmation.
            builder.setTitle("Add friend ?")
                    .setMessage("Are you sure you want to Add " + selectedOtherUser + " as your friend ?" +
                            " Ask your friend to add you back as friend")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DBRoot.child("text_me_users")
                                    .child(allUsersAndUID.get(selectedOtherUser))
                                    .child("friend_request")
                                    .child(FirebaseAuth.getInstance().getUid()).setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

                            allUsersAdapter.notifyDataSetChanged();
                            Toast.makeText(AddFriends.this, "Request sent to " + selectedOtherUser, Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            Toast.makeText(AddFriends.this, "Request Already sent to " + selectedOtherUser, Toast.LENGTH_LONG).show();
        }
    }


    private void setActionBarColour() {
        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#0a2876"));
        assert actionBar != null;
        actionBar.setBackgroundDrawable(colorDrawable);
    }
}
