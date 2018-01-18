package com.triple_a.onlinebookstore;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import components.Book;
import components.Commands;
import components.ServerInfo;
import components.User;

public class BookInfoNonEditableActivity extends AppCompatActivity {

    private Book book;
    private User user;
    private Button deleteBtn;
    private Button updateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info_non_editable);

        book = (Book) getIntent().getExtras().get(LoginAsActivity.BOOK_ACTION_UPDATE);
        user = (User) getIntent().getExtras().get(LoginAsActivity.CURRENT_USER_INFO);

        deleteBtn = findViewById(R.id.book_delete);
        updateBtn = findViewById(R.id.book_update);
        fillFields();
    }

    private void fillFields() {
        ((TextView) findViewById(R.id.book_title_n)).setText(book.getBookTitle());
        ((TextView) findViewById(R.id.book_author_n)).setText(book.getAuthorName());
        ((TextView) findViewById(R.id.book_category_n)).setText(book.getBookCategory());
        ((TextView) findViewById(R.id.book_stock_n)).setText(String.format("%d", book.getStock()));
        ((TextView) findViewById(R.id.book_price_n)).setText(String.format("%.02f", book.getPrice()));
        deleteBtn.setOnClickListener(v -> deleteAction());
        updateBtn.setOnClickListener(v -> updateAction());
    }

    private void deleteAction() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure to delete this book?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Void... voids) {
                            Socket client = ServerInfo.getClientSocket();
                            try {
                                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                                oos.flush();
                                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                                oos.writeObject(Commands.DELETE_BOOK);

                                if (ois.readObject().equals(Boolean.FALSE))
                                    return Boolean.FALSE;

                                oos.writeObject(book);
                                oos.flush();

                                if (ois.readObject().equals(Boolean.FALSE)) {
                                    Log.e("book delete", (String) ois.readObject());
                                    return Boolean.FALSE;
                                } else {
                                    Log.d("book delete", "book deleted successfully");
                                    return Boolean.TRUE;
                                }
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                                Log.e("book delete", e.getStackTrace().toString());
                            }
                            return Boolean.FALSE;
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            super.onPostExecute(aBoolean);
                            Log.d("===", "bool: " + aBoolean);
                            if (aBoolean) {
                                Toast.makeText(BookInfoNonEditableActivity.this,
                                        "Book Successfully Deleted.", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(BookInfoNonEditableActivity.this,
                                        "Book Deletion failed. try again.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }.execute();
                    Log.d("btnpressed", "yes");
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    Log.d("btnpressed", "no");
                }).show();
    }

    private void updateAction() {
        startActivityForResult(
                new Intent(this, BookInfoActivity.class)
                        .putExtra(LoginAsActivity.CURRENT_USER_INFO, user)
                        .putExtra(LoginAsActivity.BOOK_SEND, book)
                        .putExtra(LoginAsActivity.BOOK_ACTION_KEY, LoginAsActivity.BOOK_ACTION_UPDATE),
                123
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (resultCode == Activity.RESULT_OK)
                finish();
            else {
                Toast.makeText(this, "Can't Update Book. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
