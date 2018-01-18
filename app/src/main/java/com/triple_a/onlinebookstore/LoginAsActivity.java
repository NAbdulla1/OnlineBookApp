package com.triple_a.onlinebookstore;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.triple_a.onlinebookstore.utils.AlreadyLoggedIn;
import com.triple_a.onlinebookstore.utils.LogInInfoReader;

import components.UserType;

public class LoginAsActivity extends AppCompatActivity {

    public static final String USER_EMAIL_KEY = "email";
    public static final String USER_PASSWORD_KEY = "pass";
    public static final String BOOK_ACTION_KEY = "bookactionkey";
    public static final String BOOK_ACTION_ADD = "addbook";
    public static final String BOOK_ACTION_UPDATE = "updatebook";
    public static final String CURRENT_USER_INFO = "currentuserinfo";
    public static final String BOOK_SEND = "booksend";
    public static final String BOOK_SUB_SEND = "bookSubSend";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_as);

        AlreadyLoggedIn status = getLoginStatus();

        final Intent intent = new Intent();
        if (status != null && status.isAlreadyLoggedIn()) {
            if (status.getUserType() == UserType.CUSTOMER) {
                intent.setClass(this, CustomerHomeActivity.class);
            } else {
                intent.setClass(this, PublisherHomeActivity.class);
            }
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(USER_EMAIL_KEY, status.getUserEmail());
            intent.putExtra(USER_PASSWORD_KEY, status.getPassword());
            startActivity(intent);
            finish();
        } else {
            findViewById(R.id.log_in_customer_btn).setOnClickListener(view -> {
                intent.setClass(LoginAsActivity.this, CustomerLoginActivity.class);
                startActivity(intent);
                finish();
            });
            findViewById(R.id.log_in_publisher_btn).setOnClickListener(view -> {
                intent.setClass(LoginAsActivity.this, PublisherLoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private AlreadyLoggedIn getLoginStatus() {
        return LogInInfoReader.read();
    }
}
