package com.example.textme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class FriendRequests extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView friendRequestListView;
    private ArrayList<String> friendRequestArrayList;
    private ArrayAdapter<String> friendRequestListViewAdapter;
    private String selectedFriend;
    private DatabaseReference DBRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        setTitle("Accept/Reject requests");

        friendRequestListView = findViewById(R.id.lstFriendRequestAcceptOrIgnore);
        friendRequestArrayList = new ArrayList<>();
        friendRequestListView.setOnItemClickListener(FriendRequests.this);
        friendRequestListViewAdapter = new ArrayAdapter<>(FriendRequests.this, android.R.layout.simple_list_item_1, friendRequestArrayList);
        friendRequestListView.setAdapter(friendRequestListViewAdapter);
        DBRoot = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadRequestedUsers();
    }

    private void loadRequestedUsers() {
        DBRoot.child("text_me_users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .child("friend_request")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            friendRequestArrayList.add((String) d.getValue());
                            friendRequestListViewAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedFriend = friendRequestArrayList.get(position);
        DBRoot.child("text_me_users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .child("friend_request")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            if (d.getValue().equals(selectedFriend)) {
                                // Add the user to my_friends for current user
                                DBRoot.child("text_me_users")
                                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                        .child("my_friends")
                                        .child(d.getKey()).setValue(d.getValue());
                                // Add current user as friend to requested user
                                DBRoot.child("text_me_users")
                                        .child(d.getKey())
                                        .child("my_friends")
                                        .child(FirebaseAuth.getInstance().getUid())
                                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

                                // Remove the user name from friend request list for temporary case
                                friendRequestArrayList.remove(selectedFriend);
                                friendRequestListViewAdapter.notifyDataSetChanged();

                                // Remove that specific name from friend_request
                                dataSnapshot.getRef().removeValue();

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        Toast.makeText(FriendRequests.this, selectedFriend, Toast.LENGTH_LONG).show();
    }
}


