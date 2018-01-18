package com.triple_a.onlinebookstore;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import components.BookSub;
import components.Commands;
import components.Customer;
import components.Pair;
import components.ServerInfo;

public class BookSearchActivity extends AppCompatActivity {

    private AutoCompleteTextView searchBookTitle;
    private AutoCompleteTextView searchAuthor;
    private AutoCompleteTextView searchPublisher;
    private AutoCompleteTextView searchCategory;
    private Button searchButton;
    private Customer userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_search);

        userDetails = (Customer)getIntent().getExtras().get(LoginAsActivity.CURRENT_USER_INFO);
        searchBookTitle = findViewById(R.id.search_title);
        searchAuthor = findViewById(R.id.search_author);
        searchPublisher = findViewById(R.id.search_purlisher);
        searchCategory = findViewById(R.id.search_category);
        searchButton = findViewById(R.id.search_button);

        loadAutoCompleteInfo();

        searchButton.setOnClickListener(v -> {
            BookSub bs = new BookSub(searchBookTitle.getText().toString(),
                    searchAuthor.getText().toString(),
                    searchPublisher.getText().toString(),
                    searchCategory.getText().toString());
            startActivity(new Intent(this, CustomerSearchResultActivity.class)
                    .putExtra(LoginAsActivity.BOOK_SUB_SEND, bs)
                    .putExtra(LoginAsActivity.CURRENT_USER_INFO, userDetails)
            );
        });
    }

    private void loadAutoCompleteInfo() {
        new AsyncTask<Void, Void, Boolean>() {
            Pair<ArrayList<String>, ArrayList<String>> list;

            @Override
            protected Boolean doInBackground(Void... voids) {
                Socket client = ServerInfo.getClientSocket();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                    oos.flush();
                    ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                    oos.writeObject(Commands.GET_ALL_AUTHOR_CATEGORY);
                    if (ois.readObject().equals(Boolean.FALSE))
                        return Boolean.FALSE;

                    if (ois.readObject().equals(Boolean.FALSE)) {
                        Log.e("authorCategory", (String) ois.readObject());
                        return Boolean.FALSE;
                    } else {
                        Object o = ois.readObject();
                        list = (Pair<ArrayList<String>, ArrayList<String>>) o;
                        Log.e("authorCategory", "author category list found.");
                        return Boolean.TRUE;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return Boolean.FALSE;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    searchAuthor.setAdapter(new ArrayAdapter<String>(
                            BookSearchActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            list.getFirst().subList(0, list.getFirst().size())
                    ));
                    searchCategory.setAdapter(new ArrayAdapter<String>(
                            BookSearchActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            list.getSecond().subList(0, list.getSecond().size())
                    ));
                }
            }
        }.execute();

        new AsyncTask<Void, Void, Boolean>() {
            Pair<ArrayList<String>, ArrayList<String>> list;

            @Override
            protected Boolean doInBackground(Void... voids) {
                Socket client = ServerInfo.getClientSocket();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                    oos.flush();
                    ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                    oos.writeObject(Commands.GET_ALL_TITLE_PUBLISHER);
                    if (ois.readObject().equals(Boolean.FALSE))
                        return Boolean.FALSE;

                    if (ois.readObject().equals(Boolean.FALSE)) {
                        Log.e("title publisher", (String) ois.readObject());
                        return Boolean.FALSE;
                    } else {
                        Object o = ois.readObject();
                        list = (Pair<ArrayList<String>, ArrayList<String>>) o;
                        Log.e("title publisher", "title publisher list found.");
                        return Boolean.TRUE;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return Boolean.FALSE;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    searchBookTitle.setAdapter(new ArrayAdapter<String>(
                            BookSearchActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            list.getFirst().subList(0, list.getFirst().size())
                    ));
                    searchPublisher.setAdapter(new ArrayAdapter<String>(
                            BookSearchActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            list.getSecond().subList(0, list.getSecond().size())
                    ));
                }
            }
        }.execute();
    }
}
