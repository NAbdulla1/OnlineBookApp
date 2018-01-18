package com.triple_a.onlinebookstore;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.triple_a.onlinebookstore.utils.AlreadyLoggedIn;
import com.triple_a.onlinebookstore.utils.LogInInfoWriter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import components.Commands;
import components.Customer;
import components.Publisher;
import components.ServerInfo;
import components.User;
import components.UserType;

public class CustomerHomeActivity extends AppCompatActivity {

    private Button customerLogout;
    private String email;
    private Customer userDetails;
    private Button searchButton;
    private Button wishButton;
    private Button notificationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        email = getIntent().getStringExtra(LoginAsActivity.USER_EMAIL_KEY);
        searchButton = findViewById(R.id.customer_search_book_btn);
        wishButton = findViewById(R.id.customer_wish_list);
        notificationButton = findViewById(R.id.customer_notifications);
        customerLogout = findViewById(R.id.customer_logout_btn);

        userDetails = null;
        getUserDetails();

        searchButton.setOnClickListener(v->search());
        customerLogout.setOnClickListener(v -> logout());
    }

    private void search(){
        startActivity(new Intent(this, BookSearchActivity.class)
        .putExtra(LoginAsActivity.CURRENT_USER_INFO, userDetails));
    }

    private void getUserDetails() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                Socket client = ServerInfo.getClientSocket();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                    oos.flush();
                    ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                    oos.writeObject(Commands.GET_USER);
                    if (ois.readObject().equals(Boolean.FALSE))
                        return Boolean.FALSE;

                    oos.writeObject(new User(null, email, null, UserType.CUSTOMER));
                    if (ois.readObject().equals(Boolean.FALSE)) {
                        Log.e("getUser", (String) ois.readObject());
                        return Boolean.FALSE;
                    } else {
                        userDetails = (Customer) ((User) ois.readObject());
                        Log.d("getUser", "user details found.");
                        return Boolean.TRUE;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e("getUser", e.getStackTrace().toString());
                }
                return Boolean.FALSE;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (!aBoolean) {
                    userDetails = null;
                    Toast.makeText(CustomerHomeActivity.this,
                            "Can't retrieve important information. Recheck your internet connection and relaunch app.",
                            Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(CustomerHomeActivity.this,
                            "You are logged in as " + userDetails.getUserName(),
                            Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void logout(){
        LogInInfoWriter.write(new AlreadyLoggedIn(false, null, null, null));
        startActivity(new Intent(this, LoginAsActivity.class));
        finish();
    }
}
