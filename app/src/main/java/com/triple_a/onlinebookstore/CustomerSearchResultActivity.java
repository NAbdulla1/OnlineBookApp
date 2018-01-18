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
import components.BookSub;
import components.Commands;
import components.Customer;
import components.ServerInfo;

public class CustomerSearchResultActivity extends AppCompatActivity {

    private Customer userDetails;
    private ListView listView;
    private BookSub bookSub;
    private ArrayList<Book> booksList;
    private TextView errorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_search_result);

        userDetails = (Customer) getIntent().getExtras().get(LoginAsActivity.CURRENT_USER_INFO);
        bookSub = (BookSub) getIntent().getExtras().get(LoginAsActivity.BOOK_SUB_SEND);
        listView = findViewById(R.id.search_result_list_view);
        errorMsg = findViewById(R.id.errorMsg);

        populateBookList();

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            startActivity(new Intent(CustomerSearchResultActivity.this,
                    CustomerBookDetailsActivity.class)
                    .putExtra(LoginAsActivity.BOOK_SEND, booksList.get(position))
                    .putExtra(LoginAsActivity.CURRENT_USER_INFO, userDetails));
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "Long press on list item for more options.", Toast.LENGTH_LONG).show();
    }

    private void populateBookList() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                Socket client = ServerInfo.getClientSocket();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                    oos.flush();
                    ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                    oos.writeObject(Commands.GET_BOOKS_LIST_2);
                    if (ois.readObject().equals(Boolean.FALSE))
                        return Boolean.FALSE;

                    oos.writeObject(bookSub);
                    oos.flush();

                    if (ois.readObject().equals(Boolean.FALSE)) {
                        Log.e("load books list", (String) ois.readObject());
                        return Boolean.FALSE;
                    } else {
                        Object o = ois.readObject();
                        booksList = (ArrayList<Book>) o;
                        Log.d("load book list", "books loaded successfully");
                        return Boolean.TRUE;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e("load book list", e.getStackTrace().toString());
                }
                return Boolean.FALSE;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    errorMsg.setText("");
                    String bookTitles[] = new String[booksList.size()];
                    for (int i = 0; i < bookTitles.length; i++)
                        bookTitles[i] = booksList.get(i).getBookTitle();
                    listView.setAdapter(new CustomAdapter(bookTitles));
                } else {
                    errorMsg.setText("Nothing to show");
                    Toast.makeText(CustomerSearchResultActivity.this,
                            "Failed to load book data. Recheck your internet connection and relaunch app",
                            Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    class CustomAdapter extends ArrayAdapter<String> {
        CustomAdapter(String[] list) {
            super(CustomerSearchResultActivity.this, R.layout.list_row, R.id.book_title_in_list, list);
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
