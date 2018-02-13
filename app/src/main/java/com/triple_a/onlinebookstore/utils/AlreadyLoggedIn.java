package com.triple_a.onlinebookstore.utils;

import java.io.Serializable;

import components.UserType;

/**
 * Created by nayon on 15-Jan-18.
 */

public class AlreadyLoggedIn implements Serializable {
    private boolean alreadyLoggedIn;
    private String userEmail;
    private String password;
    private UserType userType;

    AlreadyLoggedIn() {
        alreadyLoggedIn = false;
        userEmail = null;
        userType = null;
        password = null;
    }

    public AlreadyLoggedIn(boolean loggedIn, String userEmail, String password, UserType userType) {
        alreadyLoggedIn = loggedIn;
        this.userEmail = userEmail;
        this.password = password;
        this.userType = userType;
    }

    public UserType getUserType() {
        return userType;
    }

    public boolean isAlreadyLoggedIn() {
        return alreadyLoggedIn;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getPassword() {
        return password;
    }
}
