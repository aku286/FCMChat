package com.android.aku.fcmchat.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.aku.fcmchat.R;
import com.android.aku.fcmchat.models.User;
import com.android.aku.fcmchat.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText email, password;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init(){

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.loading));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(true);

        email = (EditText) findViewById(R.id.edit_text_email_id);
        password = (EditText) findViewById(R.id.edit_text_password);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void registerButtonClicked(View view){
        final String password_content, email_content;
        email_content = email.getText().toString().trim();
        password_content = password.getText().toString().trim();

        mProgressDialog.show();

        //firebase not allowing to register if the password is less than 6 characters
        if(password_content.length() < 6){
            mProgressDialog.dismiss();
            Toast.makeText(this, "password should be atleast 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!TextUtils.isEmpty(email_content) && !TextUtils.isEmpty(password_content)){
            mAuth.createUserWithEmailAndPassword(email_content, password_content)
                 .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            //Successul, so add the new user to the users
                            User user = new User(firebaseUser.getUid(), firebaseUser.getEmail());

                            mDatabase.child(Constants.ARG_USERS)
                                    .child(firebaseUser.getUid())
                                    .setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                mProgressDialog.dismiss();
                                                //user successfully added
                                                Toast.makeText(getApplicationContext(),"registration is successful",Toast.LENGTH_SHORT).show();
                                                launchLoginActivity();

                                            } else {
                                                mProgressDialog.dismiss();
                                                Toast.makeText(getApplicationContext(),"registration not successful",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        }else{
                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Creating user failed",Toast.LENGTH_SHORT).show();
                        }
                    }
            });
        }else{
            mProgressDialog.dismiss();
            Toast.makeText(this,"Some fields are empty!",Toast.LENGTH_SHORT).show();
        }
    }

    public void loginButtonClicked(View view){
        launchLoginActivity();
    }

    public void launchLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
