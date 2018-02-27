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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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

    public void loginButtonClicked(View view){
        mProgressDialog.show();

        String email_content = email.getText().toString().trim();
        String password_content = password.getText().toString().trim();

        //check if the text in editText is not empty
        if(!TextUtils.isEmpty(email_content) && !TextUtils.isEmpty(password_content)){
            mAuth.signInWithEmailAndPassword(email_content, password_content)
                 .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            mProgressDialog.dismiss();
                            //launch the userlist activity
                            launchUserlistingActivity();
                        }else{
                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Username/password entered is invalid", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }else{
            mProgressDialog.dismiss();
            Toast.makeText(this, "Username/password entered is invalid", Toast.LENGTH_SHORT).show();
        }
    }

    public void launchUserlistingActivity(){
        Intent intent = new Intent(this, UserListActivity.class);
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void registerButtonClicked(View view){
        startActivity(new Intent(this, RegisterActivity.class));
    }
}
