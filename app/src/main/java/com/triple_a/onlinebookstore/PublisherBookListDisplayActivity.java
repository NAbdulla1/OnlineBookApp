package com.triple_a.onlinebookstore;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import components.Book;
import components.Commands;
import components.Publisher;
import components.ServerInfo;

import static com.triple_a.onlinebookstore.LoginAsActivity.CURRENT_USER_INFO;

public class PublisherBookListDisplayActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Book> booksList;
    private Publisher userDetails;
    private TextView errorTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publisher_book_list_display);
        listView = findViewById(R.id.publisher_list_view);
        userDetails = (Publisher) getIntent().getExtras().get(CURRENT_USER_INFO);
        errorTv = findViewById(R.id.errorview);

        listView.setOnItemClickListener((parent, view, position, id) -> startActivity(new Intent(PublisherBookListDisplayActivity.this,
                BookInfoNonEditableActivity.class)
                .putExtra(LoginAsActivity.BOOK_ACTION_UPDATE, booksList.get(position))
                .putExtra(LoginAsActivity.CURRENT_USER_INFO, userDetails)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                Socket client = ServerInfo.getClientSocket();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                    oos.flush();
                    ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                    oos.writeObject(Commands.GET_BOOKS_LIST);
                    if (ois.readObject().equals(Boolean.FALSE))
                        return Boolean.FALSE;

                    oos.writeObject(new Book(null, null, userDetails.getUserID(), null, -1, -1, null));
                    oos.flush();

                    if (ois.readObject().equals(Boolean.FALSE)) {
                        Log.e("load books listView", (String) ois.readObject());
                        return Boolean.FALSE;
                    } else {
                        Object o = ois.readObject();
                        booksList = (ArrayList<Book>) o;
                        Log.d("load book listView", "books loaded successfully");
                        return Boolean.TRUE;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e("load book listView", e.getStackTrace().toString());
                }
                return Boolean.FALSE;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                Log.d("===", "bool: " + aBoolean);
                if (aBoolean) {
                    errorTv.setText("");
                    String bookTitles[] = new String[booksList.size()];
                    for (int i = 0; i < bookTitles.length; i++)
                        bookTitles[i] = booksList.get(i).getBookTitle();
                    listView.setAdapter(new CustomAdapter(bookTitles));
                } else {
                    errorTv.setText("Nothing to show");
                    Toast.makeText(PublisherBookListDisplayActivity.this,
                            "Failed to load book data. Recheck your internet connection and relaunch app",
                            Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    class CustomAdapter extends ArrayAdapter<String> {
        CustomAdapter(String[] list) {
            super(PublisherBookListDisplayActivity.this, R.layout.list_row, R.id.book_title_in_list, list);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View row = super.getView(position, convertView, parent);

            TextView author = row.findViewById(R.id.author_name_in_list);
            TextView price = row.findViewById(R.id.price_in_list);

            author.setText("Author: " + booksList.get(position).getAuthorName());
            price.setText("Price " + booksList.get(position).getPrice());
            return row;
        }
    }
}
