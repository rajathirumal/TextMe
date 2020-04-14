package com.example.textme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.HashMap;

public class ChatListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String chatWith;
    private FirebaseAuth firebaseAuth;


    private ListView chatFriendsListView;
    private ArrayList<String> chatFriendsList;
    private ArrayAdapter chatFriendsListAdapter;

    private HashMap<Object, String> friendsAndUID;
    private DatabaseReference DBRoot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setTitle("Chat ...");
        setActionBarColour();
        firebaseAuth = FirebaseAuth.getInstance();
        DBRoot = FirebaseDatabase.getInstance().getReference();

        chatFriendsListView = findViewById(R.id.lstChatFriends);
        chatFriendsList = new ArrayList<>();
        chatFriendsListAdapter = new ArrayAdapter(ChatListActivity.this, android.R.layout.simple_list_item_1, chatFriendsList);
        chatFriendsListView.setAdapter(chatFriendsListAdapter);
        chatFriendsListView.setOnItemClickListener(ChatListActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchFriends();
    }

    private void fetchFriends() {
        friendsAndUID = new HashMap<Object, String>();
        DBRoot.child("text_me_users")
                .child(firebaseAuth.getCurrentUser().getUid())
                .child("my_friends")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            friendsAndUID.put(d.getValue(), d.getKey());
                            Log.i("usersss", "My friends " + d.getValue());
                            if (!chatFriendsList.contains(d.getValue().toString())) {
                                chatFriendsList.add(d.getValue().toString());
                            }
                            chatFriendsListAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    // Put all the menus from the "res/menu/my_menu.xml"into the option in the menu bar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Pic from the menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profileItem:
                startActivity( new Intent(ChatListActivity.this, Profile.class));
                break;

            case R.id.addFriendsItem:
                startActivity( new Intent(ChatListActivity.this, AddFriends.class));

                break;

            case R.id.acceptOrReject:
                startActivity( new Intent(ChatListActivity.this, FriendRequests.class));
                break;

            case R.id.logoutItem:
                logout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        logout();
    }

    //Logout function
    private void logout() {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(ChatListActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(ChatListActivity.this);
        }
        builder.setTitle("Delete Entry")
                .setMessage("Are you sure you want to log-out ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseAuth.signOut();
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    // Select chat with user
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        chatWith = chatFriendsList.get(position);

        Log.i("Chat with", chatWith
                + "----" + friendsAndUID.get(chatWith)
                + "----" + firebaseAuth.getCurrentUser().getDisplayName()
                + "----" + firebaseAuth.getCurrentUser().getUid());


        ChatDataSharing.currentUser = firebaseAuth.getCurrentUser().getDisplayName();
        ChatDataSharing.currentUserID = firebaseAuth.getCurrentUser().getUid();
        ChatDataSharing.chatWithUser = chatWith;
        ChatDataSharing.chatWithUserID = friendsAndUID.get(chatWith);
        startActivity( new Intent(ChatListActivity.this, FriendsChat.class));



    }


    private void setActionBarColour() {
        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#0a2876"));
        actionBar.setBackgroundDrawable(colorDrawable);
    }


}
