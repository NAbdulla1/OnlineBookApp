package com.triple_a.onlinebookstore;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import components.Book;
import components.BookOrder;
import components.Commands;
import components.Customer;
import components.ServerInfo;

import static com.triple_a.onlinebookstore.LoginAsActivity.CURRENT_USER_INFO;

public class CustomerBookOrderActivity extends AppCompatActivity {

    private Customer userDetails;
    private Book book;

    private TextView bookInfo;
    private EditText copy;
    private TextView paymentInfo;
    private Button order;
    private int orderedCopy = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_book_order);

        userDetails = (Customer) getIntent().getExtras().get(CURRENT_USER_INFO);
        book = (Book) getIntent().getExtras().get(LoginAsActivity.BOOK_SEND);

        bookInfo = findViewById(R.id.book_info);
        copy = findViewById(R.id.book_copy);
        paymentInfo = findViewById(R.id.paymentPrompt);
        order = findViewById(R.id.order);

        bookInfo.setText(String.format("Book Title: %s\n" +
                "Author: %s\n" +
                "Price: %.02f", book.getBookTitle(), book.getAuthorName(), book.getPrice()));
        copy.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double pr = (orderedCopy = Integer.parseInt(s.toString())) * book.getPrice();
                    if (pr > 0) {
                        paymentInfo.setText(String.format("Total Cost: %.02ftk", pr));
                    }
                } catch (Exception e) {
                    paymentInfo.setText("Enter valid number of copies");
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        order.setOnClickListener(v -> {
            sendOrder();
        });
    }

    private void sendOrder() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                Socket client = ServerInfo.getClientSocket();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                    oos.flush();
                    ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                    oos.writeObject(Commands.ORDER_BOOK);
                    if (ois.readObject().equals(Boolean.FALSE))
                        return Boolean.FALSE;

                    oos.writeObject(new BookOrder(userDetails, orderedCopy, book));
                    if (ois.readObject().equals(Boolean.FALSE)) {
                        Log.e("bookOrder", (String) ois.readObject());
                        return Boolean.FALSE;
                    }
                    return Boolean.TRUE;
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e("bookOrder", e.getStackTrace().toString());
                }
                return Boolean.FALSE;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (!aBoolean) {
                    Toast.makeText(CustomerBookOrderActivity.this,
                            "Can't send order. Recheck your internet connection and relaunch app.",
                            Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(CustomerBookOrderActivity.this,
                            "Order Successful. wait for response",
                            Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
}
