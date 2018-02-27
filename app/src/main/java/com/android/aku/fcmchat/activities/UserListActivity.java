package com.android.aku.fcmchat.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.aku.fcmchat.R;
import com.android.aku.fcmchat.adapters.UserListingRecyclerAdapter;
import com.android.aku.fcmchat.models.User;
import com.android.aku.fcmchat.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserListActivity extends AppCompatActivity implements UserListingRecyclerAdapter.CustomOnItemClickListener{
    private RecyclerView mUserListRecyclerView;
    private UserListingRecyclerAdapter mUserListRecyclerAdapter;

    private ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        init();
    }

    private void init(){

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.loading));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(true);

        mUserListRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_user_list);

        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        //get all users
        getUsers(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_listing, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //gets all the users registered, all of them, maybe restrict them to some in the future?
    private void getUsers(final UserListingRecyclerAdapter.CustomOnItemClickListener listener){
        mProgressDialog.show();
        mReference.child(Constants.ARG_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                List<User> users = new ArrayList<>();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    User user = dataSnapshotChild.getValue(User.class);
                    if (!TextUtils.equals(user.uid, mAuth.getCurrentUser().getUid())) {
                        users.add(user);
                    }else{
                        setTitle(user.email);
                    }
                }

                mProgressDialog.dismiss();
                //attach the adapter to the recycler view
                mUserListRecyclerAdapter = new UserListingRecyclerAdapter(users, listener);
                mUserListRecyclerView.setAdapter(mUserListRecyclerAdapter);
                mUserListRecyclerAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //send a message on failure
                mProgressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Unable to fetch users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Triggered from the menu options, logs out the current user and launches the loginactivity if successful
    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (mAuth.getCurrentUser() != null) {
                            mAuth.signOut();
                            launchLoginActivity();
                        } else {
                            //show a message that logout failed
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //Launches the login activity
    private void launchLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    //Launches the chat activity with the receiver uid and email address
    public void launchChatActivity(View v) {
        RecyclerView.ViewHolder holder = mUserListRecyclerView.getChildViewHolder(v);
        int position = holder.getAdapterPosition();

        String receiverEmail = mUserListRecyclerAdapter.getUser(position).email;
        String receiverUid = mUserListRecyclerAdapter.getUser(position).uid;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.ARG_RECEIVER, receiverEmail);
        intent.putExtra(Constants.ARG_RECEIVER_UID, receiverUid);
        startActivity(intent);
    }

    /**
     * Recycler view has no option to add item click listener, its a workaround
     * @param v
     */
    @Override
    public void onItemClicked(View v) {
        launchChatActivity(v);
    }
}
