package com.triple_a.onlinebookstore;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.triple_a.onlinebookstore.utils.AlreadyLoggedIn;
import com.triple_a.onlinebookstore.utils.LogInInfoWriter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import components.Commands;
import components.Publisher;
import components.ServerInfo;
import components.User;
import components.UserType;

import static com.triple_a.onlinebookstore.LoginAsActivity.BOOK_ACTION_KEY;
import static com.triple_a.onlinebookstore.LoginAsActivity.CURRENT_USER_INFO;
import static com.triple_a.onlinebookstore.LoginAsActivity.USER_EMAIL_KEY;

public class PublisherHomeActivity extends AppCompatActivity {
    private Button addBook;
    private Button booksList;
    private Button notifications;
    private Button publisherLogout;
    private Publisher currentUserDetails;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publisher_home);

        email = getIntent().getStringExtra(USER_EMAIL_KEY);

        addBook = findViewById(R.id.add_book_btn);
        booksList = findViewById(R.id.books_list_btn);
        notifications = findViewById(R.id.publisher_notifications_btn);
        publisherLogout = findViewById(R.id.publisher_logout_btn);

        getUserDetails();

        addBook.setOnClickListener(v -> addBookHandler(v));
        booksList.setOnClickListener(v -> showBookList());
        publisherLogout.setOnClickListener(view -> logout());
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

                    oos.writeObject(new User(null, email, null, UserType.PUBLISHER));
                    if (ois.readObject().equals(Boolean.FALSE)) {
                        Log.e("getUser", (String) ois.readObject());
                        return Boolean.FALSE;
                    } else {
                        currentUserDetails = (Publisher) ((User) ois.readObject());
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
                    currentUserDetails = null;
                    Toast.makeText(PublisherHomeActivity.this,
                            "Can't retrieve important information. Recheck your internet connection and relaunch app.",
                            Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(PublisherHomeActivity.this,
                            "You are logged in as " + currentUserDetails.getUserName(),
                            Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void addBookHandler(View v) {
        if (currentUserDetails == null) {
            Log.d("getUser", "user is null");
        }
        startActivity(new Intent(this, BookInfoActivity.class)
                .putExtra(BOOK_ACTION_KEY, LoginAsActivity.BOOK_ACTION_ADD)
                .putExtra(CURRENT_USER_INFO, currentUserDetails)
        );
    }

    private void showBookList() {
        startActivity(new Intent(this, PublisherBookListDisplayActivity.class)
                .putExtra(CURRENT_USER_INFO, currentUserDetails));
    }

    private void logout() {
        LogInInfoWriter.write(new AlreadyLoggedIn(false, null, null, null));
        startActivity(new Intent(this, LoginAsActivity.class));
        finish();
    }
}
