package com.example.textme;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class FriendsChat extends AppCompatActivity {

    private ImageView btnSendMessage;
    private EditText edtMessage;
    private LinearLayout layout;
    private ScrollView scrollView;
    private DatabaseReference DBRoot;
    private ArrayList<String> messagesIHave;
    private static int orderValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_chat);
        setTitle(ChatDataSharing.chatWithUser);

        messagesIHave = new ArrayList<>();
        layout = findViewById(R.id.layout1);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        edtMessage = findViewById(R.id.edtMessage);
        scrollView = findViewById(R.id.scrollView);
        DBRoot = FirebaseDatabase.getInstance().getReference();


        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //createMessageBubble(edtMessage.getText().toString(), new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()), true, false);
                appendDataToDB(edtMessage.getText().toString(), ChatDataSharing.currentUserID, ChatDataSharing.chatWithUserID, new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
            }
        });
    }

    private void appendDataToDB(@NotNull String message, @NotNull String fromUserID, @NotNull String toUserID, @NotNull String time) {

        DBRoot.child("text_me_users")
                .child(fromUserID)
                .child("message")
                .child(toUserID)
                .child(time + " " + orderValue + " true")
                .setValue(message);

        DBRoot.child("text_me_users")
                .child(toUserID)
                .child("message")
                .child(fromUserID)
                .child(time + " " + orderValue + " false")
                .setValue(message);

        edtMessage.getText().clear();
        orderValue++;
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPreviousMessageFromServer(ChatDataSharing.currentUserID, ChatDataSharing.chatWithUserID);
    }

    private void loadPreviousMessageFromServer(@NotNull String currentUserUID, @NotNull String fetchUserChatLogWithThisUID) {
        DBRoot.child("text_me_users")
                .child(currentUserUID)
                .child("message")
                .child(fetchUserChatLogWithThisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            String key = d.getKey();
                            if (!messagesIHave.contains(key)) {
                                assert key != null;
                                if (key.contains("true")) {
                                    Log.i("True", key.substring(8, key.length()));
                                    createMessageBubble((String) d.getValue(), key.substring(0, 8), true);
                                } else {
                                    Log.i("True", "Illa da venna");
                                    createMessageBubble((String) d.getValue(), key.substring(0, 8), false);
                                }
                                messagesIHave.add(key);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void createMessageBubble(String message, @NotNull String timeOfMessage, @NotNull boolean CurrentUser) {
        TextView userMessageTextView = new TextView(FriendsChat.this);


        userMessageTextView.setMaxWidth(900);
//        userMessageTextView.setTypeface(null, Typeface.BOLD);
        userMessageTextView.setTextSize(25);
        userMessageTextView.setTextColor(Color.parseColor("#FFFFFF"));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 7.0f;
        layoutParams.setMargins(10, 10, 10, 10);

        if (CurrentUser) {
            userMessageTextView.setText("  " + message + " ");
            layoutParams.gravity = Gravity.RIGHT;
            userMessageTextView.setGravity(Gravity.RIGHT);
            userMessageTextView.setBackgroundResource(R.drawable.bubble_in);
        }
        if (!CurrentUser) {
            userMessageTextView.setText(" " + message + "  ");
            layoutParams.gravity = Gravity.LEFT;
            userMessageTextView.setGravity(Gravity.LEFT);
            userMessageTextView.setBackgroundResource(R.drawable.bubble_out);
        }

        userMessageTextView.setLayoutParams(layoutParams);
        layout.addView(userMessageTextView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }


}
