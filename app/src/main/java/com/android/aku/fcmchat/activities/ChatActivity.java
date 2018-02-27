package com.android.aku.fcmchat.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.aku.fcmchat.R;
import com.android.aku.fcmchat.adapters.ChatRecyclerAdapter;
import com.android.aku.fcmchat.models.Chat;
import com.android.aku.fcmchat.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements TextView.OnEditorActionListener{

    private static String TAG = ChatActivity.class.getSimpleName();

    private RecyclerView mChatRecyclerView;
    private EditText mEditTextMessage;
    private ProgressDialog mProgressDialog;

    private ChatRecyclerAdapter mChatRecyclerAdapter;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendMessage();
            return true;
        }
        return false;
    }

    private void init(){

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(getIntent().getExtras().getString(Constants.ARG_RECEIVER));

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.loading));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(true);

        mEditTextMessage = (EditText) findViewById(R.id.edit_text_message);
        mEditTextMessage.setOnEditorActionListener(this);
        mChatRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_chat);
        mChatRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mChatRecyclerView.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();

        //get the messages for this user
        getMessage();
    }

    private void getMessage(){
        String receiverUid = getIntent().getExtras().getString(Constants.ARG_RECEIVER_UID);
        String senderUid = mAuth.getCurrentUser().getUid();

        getMessageFromUser(senderUid, receiverUid);
    }

    private void sendMessage() {
        String message = mEditTextMessage.getText().toString();
        String receiver = getIntent().getExtras().getString(Constants.ARG_RECEIVER);
        String receiverUid = getIntent().getExtras().getString(Constants.ARG_RECEIVER_UID);
        String sender = mAuth.getCurrentUser().getEmail();
        String senderUid = mAuth.getCurrentUser().getUid();
        //construct the chat
        Chat chat = new Chat(sender,
                receiver,
                senderUid,
                receiverUid,
                message,
                System.currentTimeMillis());

        //send the message
        sendMessageToUser(chat);
    }

    //have to move this to a separate class, too long
    private void sendMessageToUser(final Chat chat){
        final String room_type_1 = chat.senderUid + "_" + chat.receiverUid;
        final String room_type_2 = chat.receiverUid + "_" + chat.senderUid;

        //checks for the availability of a chat room with the above format and creates one if none is available, all the messages between a pair of users are
        //place in their respective chatrooms, how will you do group messaging then?
        mReference.child(Constants.ARG_CHAT_ROOMS).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(room_type_1)) {
//                    Log.e(TAG, "sendMessageToFirebaseUser: " + room_type_1 + " exists");
                    mReference.child(Constants.ARG_CHAT_ROOMS).child(room_type_1).child(String.valueOf(chat.timestamp)).setValue(chat);
                } else if (dataSnapshot.hasChild(room_type_2)) {
//                    Log.e(TAG, "sendMessageToFirebaseUser: " + room_type_2 + " exists");
                    mReference.child(Constants.ARG_CHAT_ROOMS).child(room_type_2).child(String.valueOf(chat.timestamp)).setValue(chat);
                } else {
//                    Log.e(TAG, "sendMessageToFirebaseUser: success");
                    mReference.child(Constants.ARG_CHAT_ROOMS).child(room_type_1).child(String.valueOf(chat.timestamp)).setValue(chat);
                    getMessageFromUser(chat.senderUid, chat.receiverUid);
                }
                emptyEditTextMessage();
                Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Sending message failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getMessageFromUser(String senderUid, String receiverUid) {
        final String room_type_1 = senderUid + "_" + receiverUid;
        final String room_type_2 = receiverUid + "_" + senderUid;

        mProgressDialog.show();

        //gets the messages based on the chat room type
        mReference.child(Constants.ARG_CHAT_ROOMS).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(room_type_1)) {
//                    Log.e(TAG, "getMessageFromFirebaseUser: " + room_type_1 + " exists");
                            mReference.child(Constants.ARG_CHAT_ROOMS).child(room_type_1).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            mProgressDialog.dismiss();
                            onGetMessagesSuccess(chat);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Unable to get message ", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (dataSnapshot.hasChild(room_type_2)) {
//                    Log.e(TAG, "getMessageFromFirebaseUser: " + room_type_2 + " exists");
                    mReference.child(Constants.ARG_CHAT_ROOMS).child(room_type_2).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            mProgressDialog.dismiss();
                            onGetMessagesSuccess(chat);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Unable to get message", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    mProgressDialog.dismiss();
//                    Log.e(TAG, "getMessageFromFirebaseUser: no such room available");
                    Toast.makeText(getApplicationContext(), "No such room exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgressDialog.dismiss();

                Toast.makeText(getApplicationContext(), "Unable to get message: ", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void onGetMessagesSuccess(Chat chat){
        //add it to the recycler view
        if (mChatRecyclerAdapter == null) {
            mChatRecyclerAdapter = new ChatRecyclerAdapter(new ArrayList<Chat>());
            mChatRecyclerView.setAdapter(mChatRecyclerAdapter);
        }
        mChatRecyclerAdapter.add(chat);
        mChatRecyclerView.smoothScrollToPosition(mChatRecyclerAdapter.getItemCount() - 1);
    }

    private void emptyEditTextMessage(){
        //empty the edit text
        mEditTextMessage.setText("");
    }
}
