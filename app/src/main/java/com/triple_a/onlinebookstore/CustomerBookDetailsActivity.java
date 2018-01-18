package com.triple_a.onlinebookstore;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import components.Book;
import components.Customer;

public class CustomerBookDetailsActivity extends AppCompatActivity {

    Customer userDetails;
    Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_book_details);

        userDetails = (Customer) getIntent().getExtras().get(LoginAsActivity.CURRENT_USER_INFO);
        book = (Book) getIntent().getExtras().get(LoginAsActivity.BOOK_SEND);

        fillFields();
    }

    private void fillFields() {
        ((TextView) findViewById(R.id.details_details_details_book_title_n)).setText(book.getBookTitle());
        ((TextView) findViewById(R.id.details_book_author_n)).setText(book.getAuthorName());
        ((TextView) findViewById(R.id.details_book_category_n)).setText(book.getBookCategory());
        ((TextView) findViewById(R.id.details_book_stock_n)).setText(String.format("%d", book.getStock()));
        ((TextView) findViewById(R.id.details_book_price_n)).setText(String.format("%.02f", book.getPrice()));
        ((TextView) findViewById(R.id.details_book_descr_n)).setText(book.getDescription());

        findViewById(R.id.book_get).setOnClickListener(v -> handleBookGet());
        findViewById(R.id.book_wish_list).setOnClickListener(v -> handleAddWishList());
    }

    private void handleBookGet() {
        //TODO
    }

    private void handleAddWishList() {
        //TODO
    }
}
