package com.triple_a.onlinebookstore;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import components.Book;
import components.Commands;
import components.Pair;
import components.Publisher;
import components.ServerInfo;

import static com.triple_a.onlinebookstore.LoginAsActivity.BOOK_ACTION_ADD;
import static com.triple_a.onlinebookstore.LoginAsActivity.BOOK_ACTION_KEY;
import static com.triple_a.onlinebookstore.LoginAsActivity.BOOK_ACTION_UPDATE;
import static com.triple_a.onlinebookstore.LoginAsActivity.CURRENT_USER_INFO;

public class BookInfoActivity extends AppCompatActivity {

    private EditText bookTitle;
    private AutoCompleteTextView authorName;
    private AutoCompleteTextView bookCategory;
    private EditText stock;
    private EditText price;
    private EditText descr;
    private ImageButton incr;
    private ImageButton decr;
    private Button add_update;
    private String bookAction;
    private Publisher currentUserDetails;
    private ProgressBar progressBar;
    private Book bookToUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);

        bookTitle = findViewById(R.id.book_title);
        authorName = findViewById(R.id.author_name);
        bookCategory = findViewById(R.id.book_category);
        stock = findViewById(R.id.book_count);
        price = findViewById(R.id.book_price);
        descr = findViewById(R.id.book_description);
        bookToUpdate = null;

        loadAutoCompleteInfo();

        bookAction = getIntent().getStringExtra(BOOK_ACTION_KEY);
        currentUserDetails = (Publisher) getIntent().getExtras().get(CURRENT_USER_INFO);

        incr = findViewById(R.id.count_increment_btn);
        decr = findViewById(R.id.count_decrement_btn);
        add_update = findViewById(R.id.add_update_book_btn);

        progressBar = findViewById(R.id.book_add_update_progressBar);

        boolean isUpdate = false;
        if (bookAction.equals(BOOK_ACTION_ADD)) {
            add_update.setText("Add Book");
            isUpdate = false;
        } else if (bookAction.equals(BOOK_ACTION_UPDATE)) {
            isUpdate = true;
            add_update.setText("Update Book");
            bookToUpdate = (Book) getIntent().getExtras().get(LoginAsActivity.BOOK_SEND);
            fillFields();
        }

        incr.setOnClickListener(v -> incDec(+1));
        decr.setOnClickListener(v -> incDec(-1));
        boolean finalIsUpdate = isUpdate;
        add_update.setOnClickListener(v -> bookAddUpdate(finalIsUpdate));
    }

    private void incDec(int i) {
        if (!isInteger(stock.getText().toString()))
            stock.setText("0");
        int n = Integer.parseInt(stock.getText().toString());
        n += i;
        stock.setText(String.format("%d", Math.max(0, n)));
    }

    private void fillFields() {
        bookTitle.setText(bookToUpdate.getBookTitle());
        authorName.setText(bookToUpdate.getAuthorName());
        bookCategory.setText(bookToUpdate.getBookCategory());
        stock.setText(String.format("%d", bookToUpdate.getStock()));
        price.setText(String.format("%.02f", bookToUpdate.getPrice()));
        descr.setText(bookToUpdate.getDescription());
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
                        Log.e("load security questions", (String) ois.readObject());
                        return Boolean.FALSE;
                    } else {
                        Object o = ois.readObject();
                        list = (Pair<ArrayList<String>, ArrayList<String>>) o;
                        Log.e("authorCategory", "author category listView found.");
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
                    authorName.setAdapter(new ArrayAdapter<String>(
                            BookInfoActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            list.getFirst().subList(0, list.getFirst().size())
                    ));
                    bookCategory.setAdapter(new ArrayAdapter<String>(
                            BookInfoActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            list.getSecond().subList(0, list.getSecond().size())
                    ));
                }
            }
        }.execute();
    }

    private void bookAddUpdate(boolean isUpdate) {
        if (isValidFields()) {
            if (isUpdate) {
                AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
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

                            oos.writeObject(bookToUpdate);
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
                }.execute();
                try {
                    if (task.get()) {
                        setResult(Activity.RESULT_OK);
                    } else {
                        setResult(Activity.RESULT_CANCELED);
                        return;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            Book book = new Book(
                    bookTitle.getText().toString(),
                    authorName.getText().toString(),
                    currentUserDetails.getUserID(),
                    bookCategory.getText().toString(),
                    Integer.parseInt(stock.getText().toString()),
                    Double.parseDouble(price.getText().toString()),
                    descr.getText().toString()
            );

            new AsyncTask<Void, Void, Boolean>() {
                boolean hasMsg = false;
                String msg = "";

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                protected Boolean doInBackground(Void... voids) {
                    Socket client = ServerInfo.getClientSocket();
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                        oos.flush();
                        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                        oos.writeObject(Commands.ADD_BOOK);
                        if (ois.readObject().equals(Boolean.FALSE))
                            return Boolean.FALSE;

                        oos.writeObject(book);
                        oos.flush();

                        if (ois.readObject().equals(Boolean.FALSE)) {
                            Log.e("add book", msg += (String) ois.readObject());
                            hasMsg = true;
                            return Boolean.FALSE;
                        } else {
                            Log.d("addbook", "book added.");
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
                    progressBar.setVisibility(View.INVISIBLE);
                    if (hasMsg)
                        Toast.makeText(BookInfoActivity.this, msg, Toast.LENGTH_SHORT).show();
                    if (aBoolean) {
                        Toast.makeText(BookInfoActivity.this, "Book " +
                                (isUpdate ? "Added" : "Updated") +
                                " Successfully!", Toast.LENGTH_SHORT).show();
                    }
                    if (isUpdate)
                        finish();
                }
            }.execute();
        }
    }

    private boolean isValidFields() {
        boolean f = true;
        String msg = "";
        if (bookTitle.getText().length() == 0) {
            f = false;
            msg += "\nEnter book title";
        }
        if (authorName.getText().length() == 0) {
            f = false;
            msg += "\nEnter author name";
        }
        if (bookCategory.getText().length() == 0) {
            f = false;
            msg += "\nEnter book category";
        }
        if (!isInteger(stock.getText().toString())) {
            f = false;
            msg += "\nStock should be a positive integer";
        }
        if (!isDouble(price.getText().toString())) {
            f = false;
            msg += "\nInvalid price";
        } else if (Double.parseDouble(price.getText().toString()) < 1) {
            f = false;
            msg += "\nPrice must be >= +1.00";
        }

        if (!f) {
            Toast.makeText(this, msg.substring(1), Toast.LENGTH_LONG).show();
        }
        return f;
    }

    private boolean isInteger(String num) {
        if (num.length() == 0)
            return false;
        for (char ch : num.toCharArray()) {
            if (Character.isDigit(ch) || ch == '+')
                ;
            else
                return false;
        }
        return true;
    }

    private boolean isDouble(String num) {
        if (num.length() == 0)
            return false;
        for (char ch : num.toCharArray()) {
            if (Character.isDigit(ch) || ch == '.')
                ;
            else
                return false;
        }
        return true;
    }
}
