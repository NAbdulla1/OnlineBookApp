package com.triple_a.onlinebookstore;

import android.graphics.Color;
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

import components.Commands;
import components.Customer;
import components.Notif;
import components.ServerInfo;

import static com.triple_a.onlinebookstore.LoginAsActivity.CURRENT_USER_INFO;

public class CustomerNotificationsActivity extends AppCompatActivity {

    private ListView listView;
    private Customer userDetails;
    private ArrayList<Notif> notifications;
    private TextView errorTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_notifications);
        userDetails = (Customer) getIntent().getExtras().get(CURRENT_USER_INFO);
        listView = findViewById(R.id.cus_notif_list);
        errorTv = findViewById(R.id.cerrortv);
        loadNotif();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (notifications.get(position).isNew())
                makeRead(notifications.get(position));
        });
    }

    private void loadNotif() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                Socket client = ServerInfo.getClientSocket();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                    oos.flush();
                    ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                    oos.writeObject(Commands.GET_NOTIFICATIONS);
                    if (ois.readObject().equals(Boolean.FALSE))
                        return Boolean.FALSE;

                    oos.writeObject(userDetails);
                    oos.flush();

                    if (ois.readObject().equals(Boolean.FALSE)) {
                        Log.e("load notifications list", (String) ois.readObject());
                        return Boolean.FALSE;
                    } else {
                        Object o = ois.readObject();
                        notifications = (ArrayList<Notif>) o;
                        Log.d("load notifications list", "notifications loaded successfully");
                        return Boolean.TRUE;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e("load notifications list", e.getStackTrace().toString());
                }
                return Boolean.FALSE;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                Log.d("===", "bool: " + aBoolean);
                if (aBoolean) {
                    errorTv.setText("");
                    String msgs[] = new String[notifications.size()];
                    for (int i = 0; i < msgs.length; i++)
                        msgs[i] = notifications.get(i).getMessage();
                    listView.setAdapter(new NotifListAdapter(msgs));
                    Toast.makeText(CustomerNotificationsActivity.this,
                            "The green notifications are new notifications.", Toast.LENGTH_LONG).show();
                } else {
                    errorTv.setText("Nothing to show");
                    Toast.makeText(CustomerNotificationsActivity.this,
                            "Failed to load notifications data. Recheck your internet connection and relaunch app",
                            Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    class NotifListAdapter extends ArrayAdapter<String> {
        NotifListAdapter(String[] list) {
            super(CustomerNotificationsActivity.this, android.R.layout.simple_list_item_1, list);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = super.getView(position, convertView, parent);

            if (notifications.get(position).isNew())
                v.setBackgroundColor(Color.GREEN);
            return v;
        }
    }

    private void makeRead(Notif n) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                Socket client = ServerInfo.getClientSocket();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                    oos.flush();
                    ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                    oos.writeObject(Commands.UPD_NOTIF);
                    if (ois.readObject().equals(Boolean.FALSE))
                        return Boolean.FALSE;

                    oos.writeObject(n);
                    oos.flush();

                    if (ois.readObject().equals(Boolean.FALSE)) {
                        Log.e("notification update", (String) ois.readObject());
                        return Boolean.FALSE;
                    }
                    return true;
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e("load notifications list", e.getStackTrace().toString());
                }
                return Boolean.FALSE;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                Log.d("===", "bool: " + aBoolean);
                if (aBoolean) {
                    Toast.makeText(CustomerNotificationsActivity.this,
                            "notification read", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
}
