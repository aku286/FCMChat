package com.android.aku.fcmchat.models;

/**
 * Model used for User
 */
public class User {
    public String uid;
    public String email;

    public User(){

    }

    public User(String uid, String email){
        this.uid = uid;
        this.email = email;
    }
}
