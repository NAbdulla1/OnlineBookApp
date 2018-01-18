package com.triple_a.onlinebookstore;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import components.Commands;
import components.Customer;
import components.Pair;
import components.Publisher;
import components.ServerInfo;
import components.User;
import components.UserType;

import static com.triple_a.onlinebookstore.LoginAsActivity.USER_EMAIL_KEY;
import static com.triple_a.onlinebookstore.LoginAsActivity.USER_PASSWORD_KEY;

public class PasswordRecoveryActivity extends AppCompatActivity {

    public static final String USER_TYPE_KEY = "usertypekey";
    private EditText email;
    private Spinner securityQuestion;
    private EditText securityAnswer;
    private Button recover;
    private Pair<ArrayList<Integer>, ArrayList<String>> securityQuestionsList;
    private int questionPosition;
    private UserType userType;
    private User userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        userType = (UserType) getIntent().getExtras().get(USER_TYPE_KEY);

        email = findViewById(R.id.recovery_email);
        securityQuestion = findViewById(R.id.recovery_security_question);
        securityAnswer = findViewById(R.id.recovery_security_answer);
        recover = findViewById(R.id.recovery_button);
        questionPosition = -1;

        new loadQuestions().execute();

        recover.setOnClickListener(v -> handleRecovery());

        securityQuestion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                questionPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                questionPosition = -1;
            }
        });
    }

    private class loadQuestions extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            Socket client = ServerInfo.getClientSocket();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                oos.writeObject(Commands.LOAD_SECURITY_QUESTIONS);
                if (ois.readObject().equals(Boolean.FALSE))
                    return Boolean.FALSE;

                if (ois.readObject().equals(Boolean.FALSE)) {
                    Log.e("load security questions", (String) ois.readObject());
                    return Boolean.FALSE;
                } else {
                    Object o = ois.readObject();
                    securityQuestionsList = (Pair<ArrayList<Integer>, ArrayList<String>>) o;
                    Log.d("load security question", "questions loaded successfully");
                    return Boolean.TRUE;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Log.e("load security questions", e.getStackTrace().toString());
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Log.d("===", "bool: " + aBoolean);
            if (aBoolean) {
                String[] ara = new String[securityQuestionsList.getSecond().size()];
                securityQuestionsList.getSecond().toArray(ara);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        PasswordRecoveryActivity.this, android.R.layout.simple_spinner_item, ara);
                adapter.setDropDownViewResource(R.layout.my_spinner_dropdown_item);
                securityQuestion.setAdapter(adapter);
                Log.d("spinner adapter", "adapter set");
            } else {
                Toast.makeText(PasswordRecoveryActivity.this,
                        "Failed to load spinner. Recheck your internet connection and relaunch app",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handleRecovery() {
        if (isValidFields()) {
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

                        oos.writeObject(new User(null, email.getText().toString(), null, userType));
                        if (ois.readObject().equals(Boolean.FALSE)) {
                            Log.e("getUser", (String) ois.readObject());
                            return Boolean.FALSE;
                        } else {
                            userDetails = (User) ois.readObject();
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
                    if (aBoolean) {
                        if (userDetails != null && userType == UserType.CUSTOMER) {
                            Customer customer = (Customer) userDetails;
                            if (customer.getSecurityQuestionID() == securityQuestionsList.getFirst().get(questionPosition)) {
                                if (customer.getSecurityQuestionAnswer().equals(securityAnswer.getText().toString())) {
                                    startActivityForResult(new Intent(PasswordRecoveryActivity.this, SetPasswordAfterRecoveryActivity.class)
                                            .putExtra(USER_TYPE_KEY, userType)
                                            .putExtra(USER_EMAIL_KEY, customer.getUserEmail()), 111);
                                } else
                                    Toast.makeText(PasswordRecoveryActivity.this,
                                            "Email or security question or security question answer do not match. Try Again",
                                            Toast.LENGTH_LONG).show();
                            } else
                                Toast.makeText(PasswordRecoveryActivity.this,
                                        "Email or security question or security question answer do not match. Try Again",
                                        Toast.LENGTH_LONG).show();
                        } else if(userDetails != null && userType == UserType.PUBLISHER){
                            Publisher publisher = (Publisher) userDetails;
                            if (publisher.getSecurityQuestionID() == securityQuestionsList.getFirst().get(questionPosition)) {
                                if (publisher.getSecurityQuestionAnswer().equals(securityAnswer.getText().toString())) {
                                    startActivityForResult(new Intent(PasswordRecoveryActivity.this, SetPasswordAfterRecoveryActivity.class)
                                            .putExtra(USER_TYPE_KEY, userType)
                                            .putExtra(USER_EMAIL_KEY, publisher.getUserEmail()), 111);
                                } else
                                    Toast.makeText(PasswordRecoveryActivity.this,
                                            "Email or security question or security question answer do not match. Try Again",
                                            Toast.LENGTH_LONG).show();
                            } else
                                Toast.makeText(PasswordRecoveryActivity.this,
                                        "Email or security question or security question answer do not match. Try Again",
                                        Toast.LENGTH_LONG).show();
                        }
                        else
                            Toast.makeText(PasswordRecoveryActivity.this,
                                    "Email or security question or security question answer do not match. Try Again",
                                    Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(PasswordRecoveryActivity.this,
                                "Recheck your internet connection and relaunch app.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK, new Intent()
                        .putExtra(USER_PASSWORD_KEY, data.getStringExtra(USER_PASSWORD_KEY))
                        .putExtra(USER_EMAIL_KEY, userDetails.getUserEmail())
                );
                finish();
            } else {
                Toast.makeText(this, "Operation Failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isValidFields() {
        boolean f = true;
        String msg = "";
        if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            f = false;
            msg += "\nEnter a valid email";
        }
        if (questionPosition == -1) {
            f = false;
            msg += "\nChoose a security question";
        }
        if (questionPosition != -1 && securityAnswer.getText().length() == 0) {
            f = false;
            msg += "\nGive answer of your chosen security question";
        }
        if (!f) {
            Toast.makeText(this, msg.substring(1), Toast.LENGTH_LONG).show();
        }
        return f;
    }
}
